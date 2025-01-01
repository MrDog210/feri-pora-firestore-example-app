package com.MrDog.exampleporaapp

import java.util.UUID

data class TaskItem (
    var name: String = "",
    var id: String = UUID.randomUUID().toString()
)