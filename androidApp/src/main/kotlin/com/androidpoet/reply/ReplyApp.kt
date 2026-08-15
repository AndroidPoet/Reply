package com.androidpoet.reply

import android.app.Application
import com.androidpoet.reply.di.AppGraph
import com.androidpoet.reply.di.buildAppGraph

class ReplyApp : Application() {

    lateinit var appGraph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        appGraph = buildAppGraph()
    }
}
