package org.jetbrains.rider.example.nuget.credentialprovider

import com.intellij.openapi.project.Project
import com.jetbrains.rider.framework.impl.RdSecureString
import com.jetbrains.rider.model.RdNuGetFeedCredentials
import com.jetbrains.rider.nuget.RiderNuGetHost
import com.jetbrains.rider.nuget.configs.RiderNuGetCredentialsRepo
import com.jetbrains.rider.nuget.credentials.NuGetCredentialProvider
import com.jetbrains.rider.nuget.credentials.NuGetCredentialProviderResponse
import com.jetbrains.rider.nuget.credentials.NuGetCredentialProviderStatus
import com.jetbrains.rider.nuget.credentials.NuGetCredentials
import java.net.URI

class ExampleNuGetCredentialProvider : NuGetCredentialProvider {
    companion object {
        fun supportsEndpoint(uri: URI): Boolean
                = uri.host == "acmecorp.local"
    }

    override fun getProviderResponse(project: Project, uri: URI, isRetry: Boolean): NuGetCredentialProviderResponse? {
        if (!ExampleNuGetCredentialProvider.supportsEndpoint(uri)) {
            return null
        }

        // TODO: When the caller does not mention it is retrying credential acquisition, return stored credentials.
        if (!isRetry) {
            val storedCredentials = RiderNuGetCredentialsRepo.loadCredentialsForFeed(uri.toString())
            if (storedCredentials != null) {
                return NuGetCredentialProviderResponse(
                    NuGetCredentials(storedCredentials.user, storedCredentials.password),
                    NuGetCredentialProviderStatus.Success,
                    null)
            }
        }

        // TODO: If we do have a retry, perform authentication using whatever means necessary.
        val wasSuccessful = true // let's imagine that worked...
        if (wasSuccessful) {
            // TODO: When authentication was successful, do a few things.
            val username = "username-we-just-acquired"
            val password = RdSecureString("password-or-api-key-we-just-acquired")

            // 1) Store credentials in credential store
            RiderNuGetCredentialsRepo.saveCredentialsForFeed(
                    uri.toString(), RdNuGetFeedCredentials(username, password))

            // 2) Force a reload of the NuGet configuration
            RiderNuGetHost.getInstance(project).facade.readNuGetConfig()

            // 3) Return the username/password the nuGet client can start using
            return NuGetCredentialProviderResponse(
                    NuGetCredentials(username, password),
                    NuGetCredentialProviderStatus.Success,
                    null)
        }

        // TODO: When the user cancelled, return status as such
        return NuGetCredentialProviderResponse(
            null,
            NuGetCredentialProviderStatus.UserCanceled,
            null)
    }
}

