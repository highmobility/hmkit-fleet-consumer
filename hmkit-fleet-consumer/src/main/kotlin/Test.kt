import network.WebService
import okhttp3.OkHttpClient
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.slf4j.LoggerFactory

class Test : KoinComponent {
    class MyClass {
        fun log() {
            println("loggg")
        }
    }

    val koinModules = module {
        single { MyClass() }
        single { OkHttpClient() }
    }

    val myClass by inject<MyClass>()
    val client by inject<OkHttpClient>()
    val fleetSdk = HMKitFleet.getInstance("key")

    fun start() {
        startKoin { modules(koinModules) }
    }

    fun test() {
        start()
        myClass.log()
        println(client.hashCode())
    }
}

fun main() {
    Test().test()
}