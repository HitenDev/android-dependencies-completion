package me.hiten.completion

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object Icons {


    private val map = HashMap<String, Icon>()

    init {
        map["google"] = IconLoader.getIcon("/icons/google.png")
        map["maven"] = IconLoader.getIcon("/icons/maven.png")
    }

    fun getIcon(name: String): Icon {
        return map[name] ?: return map["maven"]!!
    }
}