package com.sonda11.argus.remote.core.domain.service

import com.sonda11.argus.remote.core.domain.model.HidUsage
import com.sonda11.argus.remote.core.domain.model.KeyModifier
import com.sonda11.argus.remote.core.domain.model.KeyStroke
import com.sonda11.argus.remote.core.domain.model.SlideCommand

/**
 * Default key bindings for Reveal.js-compatible slide decks.
 *
 * Long-press commands use Alt because Reveal.js and Slidev skip fragments when
 * Alt is held. Shift jumps to the first or last slide.
 */
object ArgusKeyMap {
    fun strokeFor(command: SlideCommand): KeyStroke = when (command) {
        SlideCommand.NEXT -> KeyStroke(HidUsage.RIGHT_ARROW)
        SlideCommand.PREVIOUS -> KeyStroke(HidUsage.LEFT_ARROW)
        SlideCommand.NEXT_SLIDE -> KeyStroke(HidUsage.RIGHT_ARROW, setOf(KeyModifier.LEFT_ALT))
        SlideCommand.PREVIOUS_SLIDE -> KeyStroke(HidUsage.LEFT_ARROW, setOf(KeyModifier.LEFT_ALT))
        SlideCommand.FIRST_SLIDE -> KeyStroke(HidUsage.LEFT_ARROW, setOf(KeyModifier.LEFT_SHIFT))
        SlideCommand.LAST_SLIDE -> KeyStroke(HidUsage.RIGHT_ARROW, setOf(KeyModifier.LEFT_SHIFT))
        SlideCommand.BLACK_SCREEN -> KeyStroke(HidUsage.KEY_B)
        SlideCommand.FULL_SCREEN -> KeyStroke(HidUsage.KEY_F)
        SlideCommand.OVERVIEW -> KeyStroke(HidUsage.ESCAPE)
        SlideCommand.TOGGLE_THEME -> KeyStroke(HidUsage.KEY_T)
    }
}
