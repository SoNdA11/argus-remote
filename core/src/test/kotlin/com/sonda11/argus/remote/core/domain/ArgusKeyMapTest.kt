package com.sonda11.argus.remote.core.domain

import com.sonda11.argus.remote.core.domain.model.HidUsage
import com.sonda11.argus.remote.core.domain.model.KeyModifier
import com.sonda11.argus.remote.core.domain.model.KeyStroke
import com.sonda11.argus.remote.core.domain.model.SlideCommand
import com.sonda11.argus.remote.core.domain.service.ArgusKeyMap
import org.junit.Assert.assertEquals
import org.junit.Test

class ArgusKeyMapTest {
    @Test
    fun `next and previous use arrow keys`() {
        assertEquals(KeyStroke(HidUsage.RIGHT_ARROW), ArgusKeyMap.strokeFor(SlideCommand.NEXT))
        assertEquals(KeyStroke(HidUsage.LEFT_ARROW), ArgusKeyMap.strokeFor(SlideCommand.PREVIOUS))
    }

    @Test
    fun `slide jumps use alt so fragments are skipped`() {
        assertEquals(
            KeyStroke(HidUsage.RIGHT_ARROW, setOf(KeyModifier.LEFT_ALT)),
            ArgusKeyMap.strokeFor(SlideCommand.NEXT_SLIDE),
        )
        assertEquals(
            KeyStroke(HidUsage.LEFT_ARROW, setOf(KeyModifier.LEFT_ALT)),
            ArgusKeyMap.strokeFor(SlideCommand.PREVIOUS_SLIDE),
        )
    }

    @Test
    fun `first and last slides use shift plus arrow`() {
        assertEquals(
            KeyStroke(HidUsage.LEFT_ARROW, setOf(KeyModifier.LEFT_SHIFT)),
            ArgusKeyMap.strokeFor(SlideCommand.FIRST_SLIDE),
        )
        assertEquals(
            KeyStroke(HidUsage.RIGHT_ARROW, setOf(KeyModifier.LEFT_SHIFT)),
            ArgusKeyMap.strokeFor(SlideCommand.LAST_SLIDE),
        )
    }

    @Test
    fun `deck shortcuts map to their letters and escape`() {
        assertEquals(KeyStroke(HidUsage.KEY_B), ArgusKeyMap.strokeFor(SlideCommand.BLACK_SCREEN))
        assertEquals(KeyStroke(HidUsage.KEY_F), ArgusKeyMap.strokeFor(SlideCommand.FULL_SCREEN))
        assertEquals(KeyStroke(HidUsage.ESCAPE), ArgusKeyMap.strokeFor(SlideCommand.OVERVIEW))
        assertEquals(KeyStroke(HidUsage.KEY_T), ArgusKeyMap.strokeFor(SlideCommand.TOGGLE_THEME))
    }
}
