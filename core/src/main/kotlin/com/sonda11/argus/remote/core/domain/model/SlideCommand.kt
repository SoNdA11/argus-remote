package com.sonda11.argus.remote.core.domain.model

/**
 * Commands understood by the Reveal.js deck used in the presentation.
 */
enum class SlideCommand {
    NEXT,
    PREVIOUS,
    NEXT_SLIDE,
    PREVIOUS_SLIDE,
    FIRST_SLIDE,
    LAST_SLIDE,
    BLACK_SCREEN,
    FULL_SCREEN,
    OVERVIEW,
    TOGGLE_THEME,
}
