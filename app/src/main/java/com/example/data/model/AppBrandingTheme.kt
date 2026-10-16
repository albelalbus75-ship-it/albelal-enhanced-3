package com.example.data.model

data class AppBrandingTheme(
    val appName: String = "متجر الفيصل للتجارة العامة",
    val logoPreset: String = "gold_crown", // "gold_crown", "shield", "diamond", "store", "custom"
    val customLogoBase64: String = "",
    val themePresetId: String = "luxury_gold", // "luxury_gold", "modern_light", "royal_navy", "custom"
    val primaryColorHex: String = "#D4AF37", // Luxury Gold
    val secondaryColorHex: String = "#C9A227",
    val backgroundColorHex: String = "#0A0A0A", // Charcoal Dark
    val cardBackgroundColorHex: String = "#161616",
    val textPrimaryColorHex: String = "#FFFFFF",
    val textMutedColorHex: String = "#A0A0A0",
    val surfaceColorHex: String = "#241E0E",
    val borderColorHex: String = "#332A15",
    val phoneContact: String = "0555555555",
    val whatsappContact: String = "+966555555555",
    val emailContact: String = "info@alfaisal-store.com",
    val websiteContact: String = "www.alfaisal-store.com",
    val designerName: String = "متجر الفيصل للتجارة العامة",
    val appVersion: String = "v2.5 ERP Pro"
) {
    companion object {
        val LUXURY_GOLD = AppBrandingTheme(
            appName = "متجر الفيصل للتجارة العامة",
            themePresetId = "luxury_gold",
            primaryColorHex = "#D4AF37",
            secondaryColorHex = "#C9A227",
            backgroundColorHex = "#0A0A0A",
            cardBackgroundColorHex = "#161616",
            textPrimaryColorHex = "#FFFFFF",
            textMutedColorHex = "#A0A0A0",
            surfaceColorHex = "#241E0E",
            borderColorHex = "#332A15"
        )

        val MODERN_LIGHT = AppBrandingTheme(
            appName = "متجر الفيصل للتجارة العامة",
            themePresetId = "modern_light",
            primaryColorHex = "#1E40AF", // Modern Royal Blue / Indigo
            secondaryColorHex = "#3B82F6",
            backgroundColorHex = "#F8FAFC", // Clean light slate
            cardBackgroundColorHex = "#FFFFFF",
            textPrimaryColorHex = "#0F172A", // Dark Slate Text
            textMutedColorHex = "#64748B",
            surfaceColorHex = "#EFF6FF",
            borderColorHex = "#E2E8F0"
        )

        val ROYAL_NAVY = AppBrandingTheme(
            appName = "متجر الفيصل للتجارة العامة",
            themePresetId = "royal_navy",
            primaryColorHex = "#38BDF8", // Sky Gold / Cyan accent
            secondaryColorHex = "#0284C7",
            backgroundColorHex = "#0B132B", // Deep Navy
            cardBackgroundColorHex = "#1C2541",
            textPrimaryColorHex = "#F8FAFC",
            textMutedColorHex = "#94A3B8",
            surfaceColorHex = "#1E293B",
            borderColorHex = "#334155"
        )
    }
}
