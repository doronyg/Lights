package com.doronyg.mylightscontroller.platform.common

/**
 * Platform-specific way of handing exported Home Assistant data to the user
 * (Android: share/email intent; web: file download; ...).
 */
interface IPlatformDataExporter {
    /** Export the current HA lights data. Fire-and-forget; failures are logged. */
    fun exportHaData()
}

