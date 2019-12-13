package godopu.lab.termproject.controller.components

import android.content.Context

interface HttpResponseEventRouter {
    fun route(context : Context, code : Int, arg : String)
}