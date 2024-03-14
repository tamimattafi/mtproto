package com.attafitamim.mtproto.client.api.connection

sealed interface ConnectionType {
    val priority: Priority

    data class Upload(
        override val priority: Priority = Priority.NORMAL
    ) : ConnectionType

    data class Download(
        override val priority: Priority = Priority.MIN
    ) : ConnectionType

    data class Generic(
        val key: String? = null,
        override val priority: Priority = Priority.MAX
    ): ConnectionType

    enum class Priority {
        MAX,
        NORMAL,
        MIN
    }
}
