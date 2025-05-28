# Picture app

## Overview

Hi there! This is a project I've been working on to read the gallery and detect faces for tagging.

This Android application is designed to help manage and process images, with a particular focus on detecting faces. Tech used in project Jetpack Compose for building the user interface, Hilt to keep my dependencies organized, and Kotlin Coroutines to handle tasks in the background smoothly.

## Features

These are the set of current feature

*   It can detect faces in the images by reading gallery.
*   It shows the images after they've been processed.
*   Tagging is available for different faces which is then persisted.
*   Keep an eye on and display a list of images that have been processed, possibly highlighting any faces found.
*   Handle runtime permissions, which I need for things like accessing photos from storage or using the camera.
*   Process images in the background thread so the app stays responsive.

## Tech Stack Used

*   **UI:** Jetpack Compose – I find it a really intuitive way to build UIs.
*   **Architecture:** MVVM(Model-View-ViewModel) with Clean Architecture – This pattern helps me keep my code organized, separating concerns.
*   **Dependency Injection:** Hilt – It makes managing dependencies much simpler.
*   **Asynchronous Programming:** Kotlin Coroutines & Flow – Essential for doing work off the main thread.
*   **Image Processing:** (https://developers.google.com/ml-kit/vision/face-detection/android ML Kit android) – This is handled in my `FaceRecognizerDataSource`.
*   **Data Persistence:** (Room) – My `ImageRepository` and `ImageWithFacesEntity` model are involved here.

## How to setup the project

1.  **Clone repository**
2.  **Open it in Android Studio**
    Just open Android Studio, choose "Open an Existing Project," and find where you cloned the project.
3.  **Build the project:**
    Android Studio should sync everything up and build it. If not, you can kick off a build from the "Build" menu (Build > Make Project).

## Code structure

I've tried to follow a pretty standard Android structure:
*   `ui`: This is where all UI-related code is put – Activities, Composable functions, and ViewModels.
*   `domain`: This folder holds business logic and use cases.
*   `model`: Here you'll find data classes (like `com.example.test.model.local.ProcessedImage`) and how data access is being handled (like in `com.example.test.model.repository.ImageRepository`).
*   Hilt for dependency injection (`@HiltViewModel`, `@Inject`), which you'll see throughout the code.

## Demo link

Link - https://drive.google.com/drive/folders/15U8rnxQY9k3edHc1a1gJpo1rhdILpYKW?usp=drive_link

## Get in Touch

If you have questions or want to chat about the project, here's how you can reach me.

*   Sarthak Gandhi - sarthakgandhi1692@gmail.com
