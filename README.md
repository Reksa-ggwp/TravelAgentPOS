# TravelAgentPOS

A Point of Sale (POS) system for travel agents built with Android and Kotlin.

## Features

- **Customer Management**: Add, edit, and manage customer information
- **Trip Management**: Create and manage travel routes with driver and vehicle assignment
- **Booking System**: Seat selection and ticket booking
- **Payment Processing**: Track payments and generate receipts
- **Reports**: Generate various reports and statistics
- **Data Export**: Export data for backup and analysis

## Requirements

- **JDK**: 17 or higher
- **Android SDK**: API level 26 (Android 8.0) or higher
- **Android Studio**: Latest stable version recommended

## Setup Instructions

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd TravelAgentPOS
   ```

2. **Set up local.properties**:
   - Copy `local.properties.template` to `local.properties`
   - Update the SDK path in `local.properties` to match your Android SDK installation
   - Example: `sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk`

3. **Open in Android Studio**:
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory and select it

4. **Sync the project**:
   - Android Studio will automatically sync Gradle dependencies
   - Wait for the sync to complete

5. **Run the application**:
   - Connect an Android device or start an emulator
   - Click the "Run" button or use `Ctrl+R`

## Building the Project

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/travelagent/pos/
│   │   ├── data/           # Database entities and DAOs
│   │   ├── ui/             # Activities and UI components
│   │   ├── repository/     # Data access layer
│   │   └── utils/          # Utility classes
│   ├── res/                # Resources (layouts, strings, etc.)
│   └── AndroidManifest.xml
├── schemas/                # Room database schemas
└── build.gradle           # Module build configuration
```

## Architecture

The app follows a simple architecture pattern:
- **Activities**: Handle UI interactions
- **Repository**: Manages data access
- **Room Database**: Local data persistence
- **ViewBinding**: Type-safe view access

## Database Schema

The app uses Room database with the following main entities:
- **Customers**: Customer information
- **Trips**: Travel routes and schedules
- **Seats**: Seat availability and booking
- **Tickets**: Booking confirmations
- **Drivers**: Driver information
- **Vehicles**: Vehicle details
- **Payments**: Payment tracking

## Permissions

The app requires the following permissions:
- `INTERNET`: For potential future online features
- `BLUETOOTH`: For printer connectivity
- `WRITE_EXTERNAL_STORAGE`: For data export (Android 10 and below)
- `READ_EXTERNAL_STORAGE`: For data import (Android 10 and below)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions, please create an issue in the repository.