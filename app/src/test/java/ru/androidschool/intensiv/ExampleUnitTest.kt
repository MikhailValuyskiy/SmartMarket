package ru.androidschool.intensiv

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.properties.Delegates

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {



    @Test
    fun addition_isCorrect() {
        var v by Delegates.vetoable(1){
            prop,oldVal, newVal ->
            return@vetoable newVal <= 10
        }

        v = 100
        print(v)

        v = 9
        print(v)
    }

}
