import com.bienestaranimal.zipaquira.auth.AuthInputValidator
import com.bienestaranimal.zipaquira.auth.CitizenProfile

private fun check(condition: Boolean, message: String) {
    if (!condition) throw IllegalStateException(message)
}

fun main() {
    check(AuthInputValidator.validateLogin("ana@example.com", "12345678") == null, "valid login rejected")
    check(AuthInputValidator.validateLogin("correo-invalido", "12345678") != null, "invalid email accepted")
    check(AuthInputValidator.validateLogin("ana@example.com", "1234567") != null, "short password accepted")
    check(AuthInputValidator.validateRegistration("", "ana@example.com", "12345678") != null, "blank name accepted")

    val profile = CitizenProfile.new("uid-1", "Ana", "ANA@Example.com")
    check(profile.uid == "uid-1", "uid mismatch")
    check(profile.name == "Ana", "name mismatch")
    check(profile.email == "ana@example.com", "email must normalize")
    check(profile.roles == listOf("citizen"), "roles must be citizen only")
    check(profile.activeRole == "citizen", "active role must be citizen")
    check(profile.accountType == "citizen", "account type must be citizen")
    check(profile.status == "active", "status must be active")

    println("AUTH_DOMAIN_OK")
}
