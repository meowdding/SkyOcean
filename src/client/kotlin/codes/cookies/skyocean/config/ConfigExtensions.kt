package codes.cookies.skyocean.config

import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder.Companion.Translated
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder

var TypeBuilder.translation: String
    set(value) {
        val actualValue: String
        if (!value.startsWith("skyocean.config")) {
            actualValue = "skyocean.config.$value"
        } else {
            actualValue = value
        }

        name =  Translated(actualValue)
        description =  Translated("$actualValue.desc")
    }
    get() = ""
