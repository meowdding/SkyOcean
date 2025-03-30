package codes.cookies.skyocean.config

import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder.Companion.Translated
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder

var TypeBuilder.translation: String
    set(value) {
        name =  Translated(value)
        description =  Translated("$value.desc")
    }
    get() = ""
