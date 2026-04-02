//switch to local pc backend
/*val updateScript = file("scripts/update-backend-url.sh")
if (updateScript.exists()) {
    val useEmulator = file("local.properties").takeIf { it.exists() }?.readText()?.contains("verev.backend.useEmulator=true") == true
    val cmd = mutableListOf("bash", updateScript.absolutePath)
    if (useEmulator) cmd.add("--emulator")
    val proc = ProcessBuilder(cmd)
        .directory(projectDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
    if (proc.waitFor() != 0) {
        throw GradleException("update-backend-url.sh failed. Check scripts/update-backend-url.sh")
    }
}*/
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
