package com.alhuda.app.core.util.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

object EmptyStringAsNullSerializer :
    KSerializer<String?> by FalsyAsNullSerializer(String.serializer())
