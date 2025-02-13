# Coroutines-demo

**Run/Debug Configuration Template**

1. **Edit Configurations...**
2. Click on **Edit Configurations templates...**
3. Select **Kotlin** and set **VM options**:

```shell
-Dkotlinx.coroutines.debug
```

***

### Util

- [util/DispatcherProvider.kt](app/src/main/java/com/scarlet/util/DispatcherProvider.kt)
- [util/Resource.kt](app/src/main/java/com/scarlet/util/Resource.kt)
- [util/Utils.kt](app/src/main/java/com/scarlet/util/Utils.kt)

***

### Model

- [model/Article.kt](app/src/main/java/com/scarlet/model/Article.kt)
- [model/Recipe.kt](app/src/main/java/com/scarlet/model/Recipe.kt)
- [model/User.kt](app/src/main/java/com/scarlet/model/User.kt)

### Android

> #### Myth Buster: Suspending Functions

- [coroutines/android/MythMainActivity.kt](app/src/main/java/com/scarlet/coroutines/android/MythMainActivity.kt)

> #### Launching Coroutines in Views

- [coroutines/android/CoActivity.kt](app/src/main/java/com/scarlet/coroutines/android/CoActivity.kt)

> #### `supervisorScope` with `CoroutineExceptionHandler`

- [coroutines/android/ScopedActivity.kt](app/src/main/java/com/scarlet/coroutines/android/ScopedActivity.kt)

***

### Coroutine Basics

- [coroutines/basics/B00_WhyCoroutine.kt](app/src/main/java/com/scarlet/coroutines/basics/B00_WhyCoroutine.kt)
- [coroutines/basics/B01_ThreadVsCoroutine.kt](app/src/main/java/com/scarlet/coroutines/basics/B01_ThreadVsCoroutine.kt)
- [coroutines/basics/B02_CPS.kt](app/src/main/java/com/scarlet/coroutines/basics/B02_CPS.kt)
- [coroutines/basics/B03_RunBlocking.kt](app/src/main/java/com/scarlet/coroutines/basics/B02_CPS.kt)
- [coroutines/basics/B04_Launch.kt](app/src/main/java/com/scarlet/coroutines/basics/B04_Launch.kt)
- [coroutines/basics/B05_Async.kt](app/src/main/java/com/scarlet/coroutines/basics/B05_Async.kt)
- [coroutines/basics/B06_StructuredConcurrency.kt](app/src/main/java/com/scarlet/coroutines/basics/B06_StructuredConcurrency.kt)
- [coroutines/basics/CoroutinesDemo.kt](app/src/main/java/com/scarlet/coroutines/basics/CoroutinesDemo.kt)

***

### Coroutine Advanced Topics

- [coroutines/advanced/UnderTheHood.kt](app/src/main/java/com/scarlet/coroutines/advanced/UnderTheHood.kt)

- [coroutines/advanced/A00_SuspendOrigin.kt](app/src/main/java/com/scarlet/coroutines/advanced/A00_SuspendOrigin.kt)
- [coroutines/advanced/A01_Context.kt](app/src/main/java/com/scarlet/coroutines/advanced/A01_Context.kt)
- [coroutines/advanced/A02_CoroutineScope.kt](app/src/main/java/com/scarlet/coroutines/advanced/A02_CoroutineScope.kt)
- [coroutines/advanced/A03_JobsRelation.kt](app/src/main/java/com/scarlet/coroutines/advanced/A03_JobsRelation.kt)
- [coroutines/advanced/A04_SupervisorJob.kt](app/src/main/java/com/scarlet/coroutines/advanced/A04_SupervisorJob.kt)
- [coroutines/advanced/A05_Dispatchers.kt](app/src/main/java/com/scarlet/coroutines/advanced/A05_Dispatchers.kt)
- [coroutines/advanced/A06_CoroutineScopeFunctions.kt](app/src/main/java/com/scarlet/coroutines/advanced/A06_CoroutineScopeFunctions.kt)

    - Parallel Decomposition
        - [coroutines/advanced/parallel_decompose/A07_1_GlobalScope.kt](app/src/main/java/com/scarlet/coroutines/advanced/parallel_decompose/A07_1_GlobalScope.kt)
        - [coroutines/advanced/parallel_decompose/A07_2_GlobalScope.kt](app/src/main/java/com/scarlet/coroutines/advanced/parallel_decompose/A07_2_scopeAsParam.kt)
        - [coroutines/advanced/parallel_decompose/A07_3_GlobalScope.kt](app/src/main/java/com/scarlet/coroutines/advanced/parallel_decompose/A07_3_supervisorScope.kt)
        - [coroutines/advanced/parallel_decompose/A07_4_GlobalScope.kt](app/src/main/java/com/scarlet/coroutines/advanced/parallel_decompose/A07_4_coroutineScope.kt)
        - [coroutines/advanced/parallel_decompose/Commons.kt](app/src/main/java/com/scarlet/coroutines/advanced/parallel_decompose/Commons.kt)

***

### Miscellaneous

- [coroutines/miscs/Trampoline.kt](app/src/main/java/com/scarlet/coroutines/miscs/TrampolineDemo.kt)
- [coroutines/miscs/YieldBehavior.kt](app/src/main/java/com/scarlet/coroutines/miscs/YieldBehavior.kt)

### Coroutine Cancellation

- [coroutines/cancellation/C01_Cancellation.kt](app/src/main/java/com/scarlet/coroutines/cancellation/C01_Cancellation.kt)
- [coroutines/cancellation/C02_NonCancellable.kt](app/src/main/java/com/scarlet/coroutines/cancellation/C02_NonCancellable.kt)
- [coroutines/cancellation/C03_CooperationForCancellation.kt](app/src/main/java/com/scarlet/coroutines/cancellation/C03_CooperationForCancellation.kt)

***

### Migration from Callback to Coroutines

- [coroutines/migration/M01_CvtCallbackToSuspendFun1.kt](app/src/main/java/com/scarlet/coroutines/migration/M01_CvtCallbackToSuspendFun1.kt)
- [coroutines/migration/M02_CvtCallbackToSuspendFun2.kt](app/src/main/java/com/scarlet/coroutines/migration/M02_CvtCallbackToSuspendFun2.kt)

***

### JUnit 4 and Rules

- [junit/Junit4Test.kt](app/src/test/java/com/scarlet/junit/Junit4Test.kt)
- [junit/CustomTestRules.kt](app/src/test/java/com/scarlet/junit/CustomTestRules.kt)
- [junit/TestRulesTest.kt](app/src/test/java/com/scarlet/junit/TestRulesTest.kt)

***

### Mockk

- [mockk/ActionHandler.kt](app/src/test/java/com/scarlet/mockk/ActionHandler.kt)
- [mockk/M01_ActionHandlerTest.kt](app/src/test/java/com/scarlet/mockk/M01_ActionHandlerTest.kt)
- [mockk/M02_Mockk_CoroutinesTest.kt](app/src/test/java/com/scarlet/mockk/M02_Mockk_CoroutinesTest.kt)
- [mockk/M03_ObjectMockTest.kt](app/src/test/java/com/scarlet/mockk/M03_ObjectMockTest.kt)
- [mockk/M04_ExtensionFunctionTest.kt](app/src/test/java/com/scarlet/mockk/M04_ExtensionFunctionTest.kt)
- [mockk/MockKTest.kt](app/src/test/java/com/scarlet/mockk/MockKTest.kt)
    - Data
        - [mockk/data/Cars.kt](app/src/test/java/com/scarlet/mockk/data/Cars.kt)
        - [mockk/data/PasswordCodec.kt](app/src/test/java/com/scarlet/mockk/data/PasswordCodec.kt)
        - [mockk/data/Path.kt](app/src/test/java/com/scarlet/mockk/data/Path.kt)
        - [mockk/data/Person.kt](app/src/test/java/com/scarlet/mockk/data/Person.kt)
        - [mockk/data/Phone.kt](app/src/test/java/com/scarlet/mockk/data/Phone.kt)

***

### Testing Utilities

- [util/LiveDataTestUtil.kt (getValueForTest, captureValues, etc.)](app/src/test/java/com/scarlet/util/LiveDataTestUtil.kt)
- [util/TestUtils.kt (testDispatcher)](app/src/test/java/com/scarlet/util/TestUtils.kt)

***

### Coroutines Cancellation and Exception Handling Tests

- [coroutines/exceptions/CE01_CancellationTest.kt](app/src/test/java/com/scarlet/coroutines/exceptions/CE01_CancellationTest.kt)
- [coroutines/exceptions/CE02_LaunchEHTest.kt](app/src/test/java/com/scarlet/coroutines/exceptions/CE02_LaunchEHTest.kt)
- [coroutines/exceptions/CE03_LaunchSupervisorJobTest.kt](app/src/test/java/com/scarlet/coroutines/exceptions/CE03_LaunchSupervisorJobTest.kt)
- [coroutines/exceptions/CE04_coroutineScope_ScopeBuilderTest.kt](app/src/test/java/com/scarlet/coroutines/exceptions/CE04_coroutineScope_ScopeBuilderTest.kt)
- [coroutines/exceptions/CE05_supervisorScope_ScopeBuilderTest.kt](app/src/test/java/com/scarlet/coroutines/exceptions/CE05_supervisorScope_ScopeBuilderTest.kt)
- [coroutines/exceptions/CE06_ExceptionHandlerTest.kt](app/src/test/java/com/scarlet/coroutines/exceptions/CE06_ExceptionHandlerTest.kt)
- [coroutines/exceptions/CE07_1_AsyncEHTest.kt](app/src/test/java/com/scarlet/coroutines/exceptions/CE07_1_AsyncEHTest.kt)
- [coroutines/exceptions/CE07_2_AsyncEHTest.kt](app/src/test/java/com/scarlet/coroutines/exceptions/CE07_2_AsyncEHTest.kt)
- [coroutines/exceptions/CE08_CoroutineScopeFunctionsTest.kt](app/src/test/java/com/scarlet/coroutines/exceptions/CE08_CoroutineScopeFunctionsTest.kt)
- [coroutines/exceptions/CE09_StructuredConcurrencyTest.kt](app/src/test/java/com/scarlet/coroutines/exceptions/CE09_StructuredConcurrencyTest.kt)

***

### Test Coroutine Builder, Virtual Time Control, Test Dispatchers, and TestScope

- [coroutines/testing/intro/A01_CoroutineTest.kt](app/src/test/java/com/scarlet/coroutines/testing/intro/A01_CoroutineTest.kt)
- [coroutines/testing/intro/A02_CoroutineTest.kt](app/src/test/java/com/scarlet/coroutines/testing/intro/A02_CoroutineTest.kt)
- [coroutines/testing/intro/A03_CoroutineTest.kt](app/src/test/java/com/scarlet/coroutines/testing/intro/A03_CoroutineTest.kt)
- [coroutines/testing/intro/T01_RunBlockingVsRunTest.kt](app/src/test/java/com/scarlet/coroutines/testing/intro/T01_RunBlockingVsRunTest.kt)
- [coroutines/testing/intro/T02_VirtualTimeControlTest.kt](app/src/test/java/com/scarlet/coroutines/testing/intro/T02_VirtualTimeControlTest.kt)
- [coroutines/testing/intro/T03_Timeout01Test.kt](app/src/test/java/com/scarlet/coroutines/testing/intro/T03_Timeout01Test.kt)
- [coroutines/testing/intro/T04_Timeout02Test.kt](app/src/test/java/com/scarlet/coroutines/testing/intro/T04_Timeout02Test.kt)
- [coroutines/testing/intro/T05_MultipleDispatchersTest.kt](app/src/test/java/com/scarlet/coroutines/testing/intro/T05_MultipleDispatchersTest.kt)
- [coroutines/testing/intro/T06_BackgroundTest.kt](app/src/test/java/com/scarlet/coroutines/testing/intro/T06_BackgroundTest.kt)
- [coroutines/testing/intro/T07_CoroutineLeakTest.kt](app/src/test/java/com/scarlet/coroutines/testing/intro/T07_CoroutineLeakTest.kt)

***

### Migration from Callback to Coroutines Tests

- [coroutines/migration/CvtToSuspendingFunctionTest.kt](app/src/test/java/com/scarlet/coroutines/migration/CvtToSuspendingFunctionTest.kt)

***

### Coroutine Test Rules

- [coroutines/testing/CoroutineTestRule.kt](app/src/test/java/com/scarlet/coroutines/testing/CoroutineTestRule.kt)

***

### API Service Interface

- [coroutines/testing/ApiService.kt](app/src/test/java/com/scarlet/coroutines/testing/ApiService.kt)

***

### Coroutines Testing Exercises

- [coroutines/testing/ApiService.kt](app/src/test/java/com/scarlet/coroutines/testing/ApiService.kt)
- [coroutines/testing/version1/ArticleViewModel.kt](app/src/test/java/com/scarlet/coroutines/testing/version1/ArticleViewModel.kt)
- [coroutines/testing/version1/ArticleViewModelTest.kt](app/src/test/java/com/scarlet/coroutines/testing/version1/ArticleViewModelTest.kt)
- [coroutines/testing/version2/ArticleViewModel.kt](app/src/test/java/com/scarlet/coroutines/testing/version2/ArticleViewModel.kt)
- [coroutines/testing/version2/SetMainTest.kt](app/src/test/java/com/scarlet/coroutines/testing/version2/SetMainTest.kt)
- [coroutines/testing/version3/ArticleViewModel.kt](app/src/test/java/com/scarlet/coroutines/testing/version3/ArticleViewModel.kt)
- [coroutines/testing/version3/CoroutineTestRuleTest.kt](app/src/test/java/com/scarlet/coroutines/testing/version3/CoroutineTestRuleTest.kt)
- [coroutines/testing/version4/ArticleViewModel.kt](app/src/test/java/com/scarlet/coroutines/testing/version4/ArticleViewModel.kt)
- [coroutines/testing/version4/CoroutineTestRuleTest.kt](app/src/test/java/com/scarlet/coroutines/testing/version4/CoroutineTestRuleTest.kt)

