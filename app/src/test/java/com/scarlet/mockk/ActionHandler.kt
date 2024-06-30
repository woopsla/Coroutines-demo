package com.scarlet.mockk

interface Response {
    val isSuccessful: Boolean
    val data: String?
}

interface Callback {
    fun reply(response: Response)
}

interface Service {
    fun getResponse(request: String, callback: Callback)
}

class ActionHandler(private val service: Service) {
    var value: String? = null
        private set

    fun doRequest(request: String) {
        service.getResponse(request, object : Callback {
            override fun reply(response: Response) {
                value = if (response.isSuccessful) {
                    response.data
                } else {
                    null
                }
            }
        })
    }
}