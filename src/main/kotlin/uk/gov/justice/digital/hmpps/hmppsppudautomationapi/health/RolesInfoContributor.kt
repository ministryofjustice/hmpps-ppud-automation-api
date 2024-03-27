package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.health

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController
import java.lang.reflect.Method
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.annotation.AnnotationUtils.findAnnotation
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping

@Component
class RolesInfoContributor() : InfoContributor {

  private data class EndpointInfo(
    val method: String,
    val roleProtection: String
  )

  private data class ControllerInfo(
    val controller: String,
    val protectedEndpoints: List<EndpointInfo>,
  ) {
    override fun toString(): String {
      val endpointDescription = protectedEndpoints.joinToString(separator = "\n * ", prefix = "\n * ") { it.method }
      return "\n$controller:$endpointDescription".trimEnd()
    }
  }

  /* Information about a role, including its description and any protected endpoints */
  private data class RoleInfo(
    var description: String,
    var protectedEndpoints: MutableList<String> = mutableListOf<String>()
  )

  private fun getUserDefinedRoleInfo() : MutableMap<String,RoleInfo> {
    val roleDefintions = mutableMapOf<String,RoleInfo>()

    /* Add your role defintions here, eg
    roleDefintions["FOO_ROLE"] = RoleInfo("Some info about the foo role")
    roleDefintions["BAR_ROLE"] = RoleInfo("Some info about the bar role")
     */

    roleDefintions["ROLE_PPUD_AUTOMATION__RECALL__READWRITE"] = RoleInfo("This role is purely for the PPUD Automation API")

    return roleDefintions

  }

  override fun contribute(builder: Info.Builder) {
    val allRoleInfo = addRolesProtectingEndpointsToUserDefinedRoles(getUserDefinedRoleInfo())
    builder.withDetail("roles", allRoleInfo)
  }

  /* Merges a map of user generated RoleInfo keyed by role, with one obtained from inspecting methods
  annotated with REST-type mappings that have role protections
   */
  private fun addRolesProtectingEndpointsToUserDefinedRoles(roleInfo: MutableMap<String, RoleInfo>): MutableMap<String, RoleInfo> {

    val endpointsProtectedByRoles = getControllersWithProtectedEndpoints()
    val pattern = Regex("\\bROLE_\\w+\\b")

    endpointsProtectedByRoles.forEach() {
      it.protectedEndpoints.forEach() {
        val roles  = pattern.findAll(it.roleProtection)
        for (role in roles) {
          if (roleInfo.containsKey(role.value)) {
            roleInfo[role.value]?.description += "\nProtects endpoint " + it.method
            roleInfo[role.value]?.protectedEndpoints?.add(it.method)
          }
          else {
            roleInfo[role.value] = RoleInfo("Protects endpoint " + it.method)
            roleInfo[role.value]?.protectedEndpoints?.add(it.method)
          }
        }
      }
    }

    return roleInfo
  }

  /* Returns an array of controllers, each containing an array of its protected endpoints */
  private fun getControllersWithProtectedEndpoints() = ClassPathScanningCandidateComponentProvider(false)
    .also { it.addIncludeFilter(AnnotationTypeFilter(RestController::class.java)) }
    .findCandidateComponents("uk.gov.justice")
    .map { Class.forName(it.beanClassName) }
    .map { ControllerInfo(it.toString(), it.getProtectedEndpoints()) }

  /* Returns an array of protected endpoint methods on a controller */
  private fun Class<*>.getProtectedEndpoints() =
    this.methods
    .filter { it.isEndpoint() }
    .map { EndpointInfo(it.getVerbAndPathFromAnnotation(), getRoleProtectionForMethodFromAnnotation(it)) }
    .filter { ep -> (ep.roleProtection != "") }

  /* Determines whether this method is an endpoint */
  private fun Method.isEndpoint() = this.annotations.any {
    it.annotationClass.qualifiedName!!.startsWith("org.springframework.web.bind.annotation")
  }

  /* Returns a string containing the verb and path that is mapped to this method using eg GetMapping annotation
     eg the annotation @GetMapping('/foo/bar') will return the string "GET /foo/bar"
  */
  private fun Method.getVerbAndPathFromAnnotation(): String {

    var path = this.getDeclaredAnnotation(GetMapping::class.java)?.value?.get(0) ?: this.getDeclaredAnnotation(PostMapping::class.java)?.value?.get(0) ?: this.getDeclaredAnnotation(PutMapping::class.java)?.value?.get(0) ?: this.getDeclaredAnnotation(PatchMapping::class.java)?.value?.get(0) ?: this.getDeclaredAnnotation(DeleteMapping::class.java)?.value?.get(0)

    var requestMappingAnnotation = this.getDeclaredAnnotation(GetMapping::class.java) ?: this.getDeclaredAnnotation(PostMapping::class.java) ?: this.getDeclaredAnnotation(PutMapping::class.java) ?: this.getDeclaredAnnotation(PatchMapping::class.java) ?: this.getDeclaredAnnotation(DeleteMapping::class.java) ?: this.getDeclaredAnnotation(RequestMapping::class.java) ?: return ""

    /* The way we can infer what the verb is depends on whether we have a RequestMapping annotation (in which case we use the method) or the shortcut
    annotations (eg GetMapping), in which case we use the stringified version of the annotation
     */
    var inferrableVerbDescriptor = ""
    if(requestMappingAnnotation.toString().contains("RequestMapping")) {
      inferrableVerbDescriptor = this.getDeclaredAnnotation(RequestMapping::class.java)?.method?.get(0).toString()
    }
    else {
      inferrableVerbDescriptor = requestMappingAnnotation.toString()
    }


    /* This is just a map of descriptor strings that allow us to identify a REST verb */
    val identifierToVerbMap = mapOf(
      "GetMapping" to "GET",
      "PostMapping" to "POST",
      "PutMapping" to "PUT",
      "PatchMapping" to "PATCH",
      "DeleteMapping" to "DELETE",
      "GET" to "GET",
      "POST" to "POST",
      "PUT" to "PUT",
      "PATCH" to "PATCH",
      "DELETE" to "DELETE"
    )

    var verb = ""

    identifierToVerbMap.forEach { (identifier, v) ->
      if (inferrableVerbDescriptor.contains(identifier)) {
        verb = v
      }
    }

    return verb + " " + path
  }


  /* Returns a string containing the annotation if the Method m, or its class, is protected by a hasAnyRole or hasRole annotation
  *  eg a method annotated with @PreAuthorize(hasRole('ROLE_FOO')) will return "hasRole('ROLE_FOO')"
  *  Returns an empty string if no role protection annotation is found
  */
  private fun getRoleProtectionForMethodFromAnnotation(m: Method): String {
    val annotation = findAnnotation(m, PreAuthorize::class.java) ?: (findAnnotation(m.getDeclaringClass(), PreAuthorize::class.java)) ?: return ""
    val annotationValue = AnnotationUtils.getValue(annotation).toString()
    if (annotationValue.contains("hasAnyRole") || annotationValue.contains("hasRole")) {
      return annotationValue
    }
    return ""
  }

}
