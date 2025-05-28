# Picture app

## Overview

Hi there! This is a project I've been working on to read the gallery and detect faces for tagging.

From what I've built so far, this Android application is designed to help me manage and process images, with a particular focus on detecting faces. I'm using some modern Android development tools that I'm excited about, like Jetpack Compose for building the user interface, Hilt to keep my dependencies organized, and Kotlin Coroutines to handle tasks in the background smoothly.

## Features

Here are some of the things I've implemented or am planning to implement:

*   It can detect faces in the images I provide.
*   It shows me the images after they've been processed.
*   I've worked on managing the necessary permissions for accessing images.
*   Tagging is available for different faces which is then persisted.

Looking at my `ImageListingActivityViewModel.kt`, I've set it up to:
*   Keep an eye on and display a list of images that have been processed, possibly highlighting any faces I've found.
*   Handle runtime permissions, which I need for things like accessing photos from storage or using the camera.
*   Process images in the background so the app stays responsive.

## Tech Stack I'm Using

I've chosen to build this with:

*   **UI:** Jetpack Compose – I find it a really intuitive way to build UIs.
*   **Architecture:** MVVM (Model-View-ViewModel) – This pattern helps me keep my code organized, separating concerns.
*   **Dependency Injection:** Hilt – It makes managing dependencies much simpler.
*   **Asynchronous Programming:** Kotlin Coroutines & Flow – Essential for doing work off the main thread.
*   **Image Processing:** (https://developers.google.com/ml-kit/vision/face-detection/android ML Kit android) – This is handled in my `FaceRecognizerDataSource`.
*   **Data Persistence:** (Room) – My `ImageRepository` and `ImageWithFacesEntity` model are involved here.

## My Modules

*   **`app` (main module):** This is where most of the action happens – the main application logic, the UI I'm building, and my ViewModels.

## Getting My Project Set Up

If you want to check out my code or run the project:

1.  **Clone my repository**
2.  **Open it in Android Studio**
    Just open Android Studio, choose "Open an Existing Project," and find where you cloned the project.
3.  **Build the project:**
    Android Studio should sync everything up and build it. If not, you can kick off a build from the "Build" menu (Build > Make Project).

## How I Use It (or How You Can Use It)

1.  When I launch the app.
2.  It will ask me for permissions (like accessing photos). Grant those.
3.  Then, it should start processing images and show me any faces it detects or the processed results.

## How I've Structured My Code

I've tried to follow a pretty standard Android structure:
*   `ui`: This is where I put all my UI-related code – Activities, Composable functions, and ViewModels (like my `com.example.test.ui.mainActivity`).
*   `domain`: This folder holds my business logic and use cases.
*   `model`: Here you'll find my data classes (like `com.example.test.model.local.ProcessedImage`) and how I'm handling data access (like in `com.example.test.model.repository.ImageRepository`).
*   I'm using Hilt for dependency injection (`@HiltViewModel`, `@Inject`), which you'll see throughout the code.

## Demo link

Link - https://drive.google.com/drive/folders/15U8rnxQY9k3edHc1a1gJpo1rhdILpYKW?usp=drive_link

## Get in Touch

If you have questions or want to chat about the project, here's how you can reach me.

*   Sarthak Gandhi - sarthakgandhi1692@gmail.com
