package com.example.myapplication.data.managers

import android.content.SharedPreferences
import android.widget.Switch
import com.example.myapplication.data.models.BlockData
import com.example.myapplication.data.models.BlockTypes
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class BlockManagerTest {

    private lateinit var blockManager: BlockManager

    @Before
    fun setUp() {
        blockManager = BlockManager()
    }

    @Test
    fun addBlockIncreaseCount() {
        val view = Any()
        val data = BlockData(BlockTypes.Button, "topic/1", 10f, 20f)

        blockManager.addBlock(view, data)

        assertEquals(data, blockManager.getBlockData(view))
        assertFalse(blockManager.blockLimitReached())
    }

    @Test
    fun blockLimitReached() {
        repeat(8) {
            blockManager.addBlock(Any(), BlockData(BlockTypes.Switch, "topic/$it", 0f, 0f))
        }

        assertTrue(blockManager.blockLimitReached())
    }

    @Test
    fun removeBlockDecreaseCount() {
        val view = Any()
        val data = BlockData(BlockTypes.Switch, "topic/1", 0f, 0f)
        blockManager.addBlock(view, data)

        blockManager.removeBlock(view)

        assertNull(blockManager.getBlockData(view))
    }

    @Test
    fun saveState() {
        val switch = mockk<Switch>()
        every { switch.isChecked } returns true

        val prefs = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { prefs.edit() } returns editor

        blockManager.addBlock(switch, BlockData(BlockTypes.Switch, "topic/switch", 1f, 2f, isChecked = true))
        blockManager.saveState(prefs)

        verify { editor.putInt("blockCount", 1) }
        verify { editor.putString("block_0-type", "switch") }
        verify { editor.putBoolean("block_0-checked", true) }
        verify { editor.putString("block_0-topic", "topic/switch") }
        verify { editor.putFloat("block_0-x", 1f) }
        verify { editor.putFloat("block_0-y", 2f) }
        verify { editor.apply() }
    }

    @Test
    fun restoreState() {
        val prefs = mockk<SharedPreferences>()
        every { prefs.getInt("blockCount", 0) } returns 2

        every { prefs.getString("block_0-type", null) } returns "switch"
        every { prefs.getString("block_0-topic", null) } returns "topic/1"
        every { prefs.getFloat("block_0-x", 0f) } returns 1f
        every { prefs.getFloat("block_0-y", 0f) } returns 2f
        every { prefs.getBoolean("block_0-checked", false) } returns true

        every { prefs.getString("block_1-type", null) } returns "button"
        every { prefs.getString("block_1-topic", null) } returns "topic/2"
        every { prefs.getFloat("block_1-x", 0f) } returns 3f
        every { prefs.getFloat("block_1-y", 0f) } returns 4f

        val restored = blockManager.restoreState(prefs)

        assertEquals(2, restored.size)

        val switchBlock = restored[0]
        assertEquals("topic/1", switchBlock.topic)
        assertEquals(1f, switchBlock.x)
        assertEquals(2f, switchBlock.y)
        assertEquals(true, switchBlock.isChecked)
        assertEquals(BlockTypes.Switch, switchBlock.type)

        val buttonBlock = restored[1]
        assertEquals("topic/2", buttonBlock.topic)
        assertEquals(3f, buttonBlock.x)
        assertEquals(4f, buttonBlock.y)
        assertEquals(BlockTypes.Button, buttonBlock.type)
    }


}