package com.paintedmap.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalCoroutinesApi::class)
class DelayTest {

    // внутри runTest задержка delay(5000) не тормозит выполнение,
    // а перематывает виртуальные часы на 5000 мс вперёд
    @Test
    fun testSkip() = runTest {
        val realTime = measureTimeMillis {
            delay(5_000L)
        }
        assertEquals(5_000L, currentTime)
        assertTrue(realTime < 500L)
    }

    // виртуальным временем можно управлять через advanceTimeBy,
    // чтобы проверить состояние программы до и после завершения задержки.
    @Test
    fun testTime() = runTest {
        var text = "start"

        launch {
            delay(300L)
            text = "done"
        }

        advanceTimeBy(200L)
        runCurrent()
        assertEquals("start", text)

        advanceTimeBy(100L)
        runCurrent()
        assertEquals("done", text)
    }

    // при переключении на Dispatchers.IO виртуальные часы перестают работать —
    // корутина уходит в реальный фоновый поток и ждёт настоящее физическое время.
    @Test
    fun testDispatcher() = runTest {
        val realTime = measureTimeMillis {
            withContext(Dispatchers.IO) {
                delay(150L)
            }
        }
        assertEquals(0L, currentTime)
        assertTrue(realTime >= 140L)
    }
}
