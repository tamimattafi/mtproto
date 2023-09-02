package com.attafitamim.mtproto.client.api.connection

object ConnectionFlags {
    const val ConnectionTypeGeneric = 1
    const val ConnectionTypeDownload = 2
    const val ConnectionTypeUpload = 4
    const val ConnectionTypePush = 8
    const val ConnectionTypeDownload2 = ConnectionTypeDownload or (1 shl 16)

    const val FileTypePhoto = 0x01000000
    const val FileTypeVideo = 0x02000000
    const val FileTypeAudio = 0x03000000
    const val FileTypeFile = 0x04000000

    const val RequestFlagEnableUnauthorized = 1
    const val RequestFlagFailOnServerErrors = 2
    const val RequestFlagCanCompress = 4
    const val RequestFlagWithoutLogin = 8
    const val RequestFlagTryDifferentDc = 16
    const val RequestFlagForceDownload = 32
    const val RequestFlagInvokeAfter = 64
    const val RequestFlagNeedQuickAck = 128

    const val ConnectionStateConnecting = 1
    const val ConnectionStateWaitingForNetwork = 2
    const val ConnectionStateConnected = 3
    const val ConnectionStateConnectingToProxy = 4
    const val ConnectionStateUpdating = 5

    const val DEFAULT_DATACENTER_ID = Int.MAX_VALUE
}