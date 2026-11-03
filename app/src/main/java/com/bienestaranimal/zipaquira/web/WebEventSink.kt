package com.bienestaranimal.zipaquira.web

interface WebEventSink {
    fun emit(eventName: String, payloadJson: String)
}
