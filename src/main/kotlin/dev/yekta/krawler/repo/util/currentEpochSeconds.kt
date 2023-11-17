package dev.yekta.krawler.repo.util

import kotlinx.datetime.Clock.System.now

fun currentEpochSeconds() = now().epochSeconds
