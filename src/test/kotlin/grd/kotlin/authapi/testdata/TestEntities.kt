package grd.kotlin.authapi.testdata

import grd.kotlin.authapi.enums.*
import grd.kotlin.authapi.models.*

open class TestEntities
{
    val userModerator = AUser(id = "test-userModerator-id",
        added = "2021-03-26T22:00:00.000Z",
        deleted = null,
        email = "userModerator@example.com",
        username = "userModerator",
        password = "\$2a\$10\$OlxiBA58Aaj0uwRzR6ZMV.AZ6/bRQT91GsAc24G5NKZF6LkBYqzV.",
        about = "I am a moderator",
        role = UserRole.MODERATOR,
        expirationTime = null,
        usersRecruited = 11,
        moderatorComments = mutableMapOf(Pair("2021-03-26T22:00:00.000Z", "Created for testing")),
        lastActiveTime = "2021-03-26T22:00:00.000Z",
        lockedUntilTime = null,
        previousUsernames = mutableMapOf(),
        usernameChangeTime = "2021-03-26T22:00:00.000Z",
        profilePicture = null,)
    val userNormal = AUser(id = "test-userNormal-id",
        added = "2021-03-26T22:00:00.000Z",
        deleted = null,
        email = "userNormal@example.com",
        username = "userNormal",
        password = "\$2a\$10\$OlxiBA58Aaj0uwRzR6ZMV.AZ6/bRQT91GsAc24G5NKZF6LkBYqzV.",
        about = "I am a normal user",
        role = UserRole.USER,
        expirationTime = null,
        usersRecruited = 2,
        moderatorComments = mutableMapOf(Pair("2021-03-26T22:00:00.000Z", "Created for testing")),
        lastActiveTime = "2021-03-26T22:00:00.000Z",
        lockedUntilTime = null,
        previousUsernames = mutableMapOf(),
        usernameChangeTime = "2021-03-26T22:00:00.000Z",
        profilePicture = null,)
    val userOther = AUser(id = "test-userOther-id",
        added = "2021-03-26T22:00:00.000Z",
        deleted = null,
        email = "userOther@example.com",
        username = "userOther",
        password = "\$2a\$10\$OlxiBA58Aaj0uwRzR6ZMV.AZ6/bRQT91GsAc24G5NKZF6LkBYqzV.",
        about = "I am some other user",
        role = UserRole.OTHER,
        expirationTime = null,
        usersRecruited = 0,
        moderatorComments = mutableMapOf(Pair("2021-03-26T22:00:00.000Z", "Created for testing")),
        lastActiveTime = "2021-03-26T22:00:00.000Z",
        lockedUntilTime = null,
        previousUsernames = mutableMapOf(),
        usernameChangeTime = "2021-03-26T22:00:00.000Z",
        profilePicture = null,)
    val clearTextPassword = "password"

    private operator fun Float?.plus(second: Float?): Float?
    {
        if(this == null || second == null)
            return null

        return this + second
    }
    private operator fun Int?.plus(second: Int?): Int?
    {
        if(this == null || second == null)
            return null

        return this + second
    }
}
