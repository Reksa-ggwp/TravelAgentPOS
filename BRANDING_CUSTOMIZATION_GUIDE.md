# 🎨 BRANDING CUSTOMIZATION GUIDE

This guide helps you customize the app with your client's company branding (name, logo, colors).

---

## 📱 1. APP NAME

### Current: "Travel Agent POS"

### Files to Change:

#### ✅ **app/src/main/res/values/strings.xml**
```xml
<resources>
    <!-- TODO: CHANGE APP NAME HERE -->
    <!-- Max length: 30 characters recommended for proper display -->
    <!-- Example: "ABC Travel Express" or "XYZ Transport POS" -->
    <string name="app_name">Travel Agent POS</string>
    
    <!-- Rest of strings... -->
</resources>
```

#### ✅ **app/src/main/res/layout/nav_header.xml**
```xml
<!-- TODO: CHANGE APP NAME IN NAVIGATION DRAWER -->
<!-- Line ~30 -->
<TextView
    android:text="TRAVEL AGENT POS"  <!-- CHANGE THIS -->
    android:textSize="24sp"
    android:textStyle="bold"/>

<TextView
    android:text="Sistem Manajemen Perjalanan"  <!-- CHANGE TAGLINE HERE -->
    android:textSize="14sp"/>
```

#### ✅ **app/src/main/res/layout/activity_splash.xml**
```xml
<!-- TODO: CHANGE APP NAME ON SPLASH SCREEN -->
<!-- Line ~32 -->
<TextView
    android:id="@+id/tvAppName"
    android:text="TRAVEL AGENT POS"  <!-- CHANGE THIS -->
    android:textSize="28sp"/>

<TextView
    android:id="@+id/tvTagline"
    android:text="Sistem Manajemen Perjalanan"  <!-- CHANGE TAGLINE -->
    android:textSize="14sp"/>
```

---

## 🖼️ 2. APP ICONS & LOGOS

### A. APP LAUNCHER ICON (Home Screen Icon)

**Location:** `app/src/main/res/mipmap-*/`

**Required Files:**
- `mipmap-mdpi/ic_launcher.png` (48x48 px)
- `mipmap-hdpi/ic_launcher.png` (72x72 px)
- `mipmap-xhdpi/ic_launcher.png` (96x96 px)
- `mipmap-xxhdpi/ic_launcher.png` (144x144 px)
- `mipmap-xxxhdpi/ic_launcher.png` (192x192 px)

**Specifications:**
- **Format:** PNG (with transparency) or WEBP
- **Shape:** Can be any shape, Android will crop to circle/square based on device
- **Colors:** Full color, avoid transparency in important areas
- **Design:** Keep important content in center (safe zone)

**📝 TODO Steps:**
1. Design your icon (recommend using Android Studio's Asset Studio)
2. Export in all 5 sizes listed above
3. Replace existing `ic_launcher.png` files in each `mipmap-*` folder
4. Replace `ic_launcher_round.png` if you want round variant

**Tools:**
- Android Studio: Right-click `res` → New → Image Asset
- Online: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html

---

### B. BUS ICON (Used Throughout App)

**Location:** `app/src/main/res/drawable/ic_bus.xml`

**Current Usage:**
- Splash screen logo
- Navigation drawer header
- Trip-related screens
- Empty state illustrations

**Specifications:**
- **Format:** Vector Drawable (XML) - RECOMMENDED
- **Alternative:** PNG in `drawable-*/` folders
- **Size (Vector):** 24x24 dp (standard)
- **Size (PNG):** 
  - drawable-mdpi: 24x24 px
  - drawable-hdpi: 36x36 px
  - drawable-xhdpi: 48x48 px
  - drawable-xxhdpi: 72x72 px
  - drawable-xxxhdpi: 96x96 px
- **Color:** Single color (tinted dynamically in code)

**📝 TODO Steps:**

**Option 1: Vector Drawable (Recommended)**
```xml
<!-- TODO: REPLACE ic_bus.xml WITH YOUR COMPANY LOGO -->
<!-- File: app/src/main/res/drawable/ic_bus.xml -->
<!-- 
INSTRUCTIONS:
1. Convert your logo to SVG format
2. Use Android Studio: Right-click 'drawable' → New → Vector Asset
3. Choose 'Local file (SVG, PSD)' and select your logo
4. Name it 'ic_bus' to replace existing
5. Adjust size to 24dp x 24dp

TOOLS:
- Convert to SVG: https://convertio.co/png-svg/
- Optimize SVG: https://jakearchibald.github.io/svgomg/
-->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <!-- Current bus icon path -->
    <path android:fillColor="@color/info" .../>
</vector>
```

**Option 2: PNG Images**
```
TODO: REPLACE PNG LOGO FILES
Location: app/src/main/res/drawable-*/ic_bus.png

Create 5 versions:
- drawable-mdpi/ic_bus.png (24x24 px)
- drawable-hdpi/ic_bus.png (36x36 px)
- drawable-xhdpi/ic_bus.png (48x48 px)
- drawable-xxhdpi/ic_bus.png (72x72 px)
- drawable-xxxhdpi/ic_bus.png (96x96 px)

Note: Keep PNG background transparent
```

---

### C. OTHER ICONS TO CUSTOMIZE (Optional)

```
📁 app/src/main/res/drawable/

TODO: Consider replacing these icons with your brand style:

ic_person.xml       - Customer icon
ic_driver.xml       - Driver icon
ic_location.xml     - Location pin
ic_calendar.xml     - Date icon
ic_phone.xml        - Phone icon
ic_arrow_right.xml  - Navigation arrow

All follow same specs as ic_bus.xml (24dp vector or 24-96px PNG)
```

---

## 🎨 3. BRAND COLORS

**Location:** `app/src/main/res/values/colors.xml`

```xml
<!-- TODO: CUSTOMIZE BRAND COLORS -->
<!-- 
CURRENT THEME: Blue & Orange
CHANGE THESE TO MATCH YOUR CLIENT'S BRAND COLORS

Use online picker: https://m2.material.io/design/color/the-color-system.html
-->

<resources>
    <!-- TODO: PRIMARY BRAND COLOR (Main app color) -->
    <!-- Used in: Toolbar, buttons, accents -->
    <color name="primary">#1976D2</color>           <!-- Change this -->
    <color name="primary_dark">#0D47A1</color>       <!-- Darker shade -->
    <color name="primary_light">#BBDEFB</color>      <!-- Lighter shade -->
    
    <!-- TODO: SECONDARY/ACCENT COLOR -->
    <!-- Used in: FAB button, highlights -->
    <color name="accent">#FF6F00</color>             <!-- Change this -->
    <color name="accent_light">#FFB74D</color>       <!-- Lighter shade -->

    <!-- Semantic colors (usually don't change) -->
    <color name="success">#4CAF50</color>    <!-- Green - Keep -->
    <color name="warning">#FF9800</color>    <!-- Orange - Keep -->
    <color name="error">#F44336</color>      <!-- Red - Keep -->
    <color name="info">#2196F3</color>       <!-- Blue - Keep -->

    <!-- ... rest of colors ... -->
</resources>
```

**How to Choose Colors:**
1. Get your client's brand HEX color (e.g., #FF5722)
2. Generate shades using: https://m2.material.io/design/color/
3. Replace `primary`, `primary_dark`, `primary_light`
4. Optionally replace `accent` color

---

## 📝 4. COMPLETE CHECKLIST

### ✅ App Name Changes
- [ ] `strings.xml` - app_name
- [ ] `nav_header.xml` - Navigation drawer title
- [ ] `activity_splash.xml` - Splash screen title

### ✅ Icon Changes
- [ ] `mipmap-*/ic_launcher.png` - Launcher icon (5 sizes)
- [ ] `mipmap-*/ic_launcher_round.png` - Round variant (5 sizes)
- [ ] `drawable/ic_bus.xml` - Main logo (vector or PNG)

### ✅ Color Changes (Optional)
- [ ] `colors.xml` - primary color
- [ ] `colors.xml` - primary_dark color
- [ ] `colors.xml` - primary_light color
- [ ] `colors.xml` - accent color

### ✅ Testing After Changes
- [ ] Build app: `./gradlew assembleDebug`
- [ ] Check home screen icon
- [ ] Open app and check splash screen
- [ ] Navigate drawer - check logo & name
- [ ] Check all screens for proper colors

---

## 🔧 5. RECOMMENDED TOOLS

### Icon Creation
- **Android Studio Asset Studio** (Built-in)
- **Figma** (Free design tool)
- **Inkscape** (Free vector editor)

### Icon Generators
- https://romannurik.github.io/AndroidAssetStudio/
- https://easyappicon.com/
- https://appicon.co/

### Color Tools
- https://m2.material.io/design/color/
- https://coolors.co/
- https://color.adobe.com/

### Format Converters
- PNG to WebP: https://cloudconvert.com/png-to-webp
- Any to SVG: https://convertio.co/image-converter/

---

## ⚠️ IMPORTANT NOTES

### File Naming Rules
- Use lowercase only
- Use underscores, not spaces: `company_logo.png` ✅ `Company Logo.png` ❌
- No special characters except underscore

### Size Limits
- **App Name:** Max 30 characters (for proper display)
- **Vector Icons:** Keep under 10 KB for performance
- **PNG Icons:** Total all sizes should be under 500 KB

### Testing
Always test on both:
- Physical device
- Emulator with different screen sizes
- Check in both light and dark mode (if supported)
- Portrait and landscape orientations

### Backup Original Files
Before replacing any files, create a backup:
```bash
# Create backup folder
mkdir branding_backup

# Copy original files
cp app/src/main/res/values/strings.xml branding_backup/
cp -r app/src/main/res/mipmap-* branding_backup/
cp app/src/main/res/drawable/ic_bus.xml branding_backup/
```

---

## 🚀 6. QUICK START GUIDE

### Minimum Changes (5 minutes)
1. ✅ Change app name in `strings.xml`
2. ✅ Replace `ic_launcher.png` in all `mipmap-*` folders
3. ✅ Build and test

### Full Branding (30 minutes)
1. ✅ Do all minimum changes
2. ✅ Replace `ic_bus.xml` with company logo
3. ✅ Update colors in `colors.xml`
4. ✅ Change splash screen and nav drawer text
5. ✅ Full testing on device

---

## 📧 7. FILE LOCATIONS SUMMARY

```
📦 TravelAgentPOS/
├── 📁 app/src/main/
│   ├── 📁 res/
│   │   ├── 📁 values/
│   │   │   ├── strings.xml          ← TODO: App name here
│   │   │   └── colors.xml           ← TODO: Brand colors here
│   │   │
│   │   ├── 📁 layout/
│   │   │   ├── activity_splash.xml  ← TODO: Splash screen text
│   │   │   └── nav_header.xml       ← TODO: Drawer header text
│   │   │
│   │   ├── 📁 drawable/
│   │   │   └── ic_bus.xml           ← TODO: Main logo here
│   │   │
│   │   └── 📁 mipmap-*/
│   │       └── ic_launcher.png      ← TODO: App icon (5 sizes)
│   │
│   └── AndroidManifest.xml          ← (No changes needed)
```

---

## 🎯 8. EXAMPLE: Complete Rebranding

### Example Client: "ABC Express Transport"

#### Step 1: strings.xml
```xml
<string name="app_name">ABC Express</string>
```

#### Step 2: colors.xml
```xml
<!-- ABC Express brand: Red & Gold -->
<color name="primary">#D32F2F</color>
<color name="primary_dark">#B71C1C</color>
<color name="primary_light">#FFCDD2</color>
<color name="accent">#FFC107</color>
<color name="accent_light">#FFECB3</color>
```

#### Step 3: nav_header.xml
```xml
<TextView android:text="ABC EXPRESS"/>
<TextView android:text="Sistem Transport Terpercaya"/>
```

#### Step 4: activity_splash.xml
```xml
<TextView android:text="ABC EXPRESS"/>
<TextView android:text="Sistem Transport Terpercaya"/>
```

#### Step 5: Replace Icons
- Created `abc_logo.svg` → Converted to `ic_bus.xml`
- Created app icon → Replaced all `ic_launcher.png` files

#### Step 6: Build & Test
```bash
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

---

## 🐛 9. TROUBLESHOOTING

### Problem: App name not changing
**Solution:** Clean and rebuild
```bash
./gradlew clean
./gradlew assembleDebug
```

### Problem: Icon not showing correctly
**Solution:** 
1. Clear app data: Settings → Apps → Your App → Clear Data
2. Uninstall and reinstall app
3. Check all 5 icon sizes are present

### Problem: Colors not applying
**Solution:**
1. Make sure you changed colors in `values/colors.xml`
2. Check you didn't accidentally edit `values-night/colors.xml`
3. Clean and rebuild project

### Problem: Vector drawable not rendering
**Solution:**
1. Ensure vector is valid XML
2. Keep path data simple (no complex gradients)
3. Alternative: Use PNG format instead

---

## 📞 10. SUPPORT RESOURCES

### Android Documentation
- Icons: https://developer.android.com/guide/practices/ui_guidelines/icon_design
- Colors: https://developer.android.com/guide/topics/ui/look-and-feel/themes
- Assets: https://developer.android.com/studio/write/image-asset-studio

### Design Resources
- Material Design: https://m2.material.io/
- Icon packs: https://fonts.google.com/icons
- Color palettes: https://materialui.co/colors

### Community Help
- Stack Overflow: https://stackoverflow.com/questions/tagged/android
- Reddit: r/androiddev
- Android Developers Discord

---

## ✅ FINAL CHECKLIST

Before delivering to client:

- [ ] All app names changed consistently
- [ ] App launcher icon replaced (all 5 sizes)
- [ ] Company logo added (ic_bus)
- [ ] Brand colors applied
- [ ] Splash screen updated
- [ ] Navigation drawer updated
- [ ] Built debug APK successfully
- [ ] Tested on physical device
- [ ] Tested on tablet (if applicable)
- [ ] Screenshots taken for client approval
- [ ] Original files backed up
- [ ] Documentation updated

---

## 📝 NOTES FOR DEVELOPERS

### Version Control
```bash
# Commit branding changes separately
git add app/src/main/res/values/strings.xml
git add app/src/main/res/values/colors.xml
git add app/src/main/res/mipmap-*/
git add app/src/main/res/drawable/ic_bus.*
git commit -m "feat: Apply client branding - [Client Name]"
```

### Multiple Clients
If managing multiple clients, use Git branches:
```bash
# Create client branch
git checkout -b client/abc-express

# Make branding changes
# ...

# Create another client branch from main
git checkout main
git checkout -b client/xyz-transport
```

### Build Variants (Advanced)
For multiple clients from same codebase, use product flavors:
```gradle
// app/build.gradle
android {
    flavorDimensions "client"
    productFlavors {
        abcexpress {
            dimension "client"
            applicationId "com.abcexpress.pos"
            resValue "string", "app_name", "ABC Express"
        }
        xyztransport {
            dimension "client"
            applicationId "com.xyztransport.pos"
            resValue "string", "app_name", "XYZ Transport"
        }
    }
}
```

---

## 🎉 YOU'RE DONE!

After completing all TODO items:
1. Build release APK
2. Test thoroughly
3. Get client approval
4. Deploy to production

**Good luck with your customization! 🚀**