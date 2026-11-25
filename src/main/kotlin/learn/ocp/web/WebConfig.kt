package learn.ocp.web

import learn.ocp.content.ContentRepository
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/** Serves images referenced from markdown (e.g. practice/ocp17-829/img/...). */
@Configuration
class WebConfig(private val content: ContentRepository) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        listOf("practice", "docs").forEach { dir ->
            registry.addResourceHandler("/content/$dir/**")
                .addResourceLocations(content.root.resolve(dir).toUri().toString().let { if (it.endsWith("/")) it else "$it/" })
        }
    }
}
