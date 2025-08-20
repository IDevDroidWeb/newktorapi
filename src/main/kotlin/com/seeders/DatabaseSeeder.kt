package com.seeders

import com.models.*
import com.repositories.*
import com.utils.Constants
import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt

object DatabaseSeeder {

    suspend fun seedAll() {
        println("Starting database seeding...")

        seedCountries()
        seedGovernorates()
        seedSpecifications()
        seedSubscriptionPlans()
        seedOnboarding()
        seedUsers()
        seedProperties()

        println("Database seeding completed!")
    }

    private suspend fun seedCountries() {
        val countryRepo = CountryRepository()

        val countries = listOf(
            Country(nameAr = "مصر", nameEn = "Egypt", flagImage = "https://example.com/flags/egypt.png"),
            Country(nameAr = "السعودية", nameEn = "Saudi Arabia", flagImage = "https://example.com/flags/saudi.png"),
            Country(nameAr = "الإمارات", nameEn = "UAE", flagImage = "https://example.com/flags/uae.png"),
            Country(nameAr = "الكويت", nameEn = "Kuwait", flagImage = "https://example.com/flags/kuwait.png")
        )

        countries.forEach { countryRepo.create(it) }
        println("Countries seeded")
    }

    private suspend fun seedGovernorates() {
        val governorateRepo = GovernorateRepository()
        val countryRepo = CountryRepository()

        val egypt = countryRepo.findAll().first { it.nameEn == "Egypt" }

        val governorates = listOf(
            Governorate(countryId = egypt.id, nameAr = "القاهرة", nameEn = "Cairo"),
            Governorate(countryId = egypt.id, nameAr = "الجيزة", nameEn = "Giza"),
            Governorate(countryId = egypt.id, nameAr = "الإسكندرية", nameEn = "Alexandria"),
            Governorate(countryId = egypt.id, nameAr = "الغردقة", nameEn = "Hurghada")
        )

        governorates.forEach { governorateRepo.create(it) }
        println("Governorates seeded")
    }

    private suspend fun seedSpecifications() {
        val specRepo = SpecificationRepository()

        val specifications = listOf(
            Specification(nameAr = "حمام سباحة", nameEn = "Swimming Pool", iconImage = "https://example.com/icons/pool.png"),
            Specification(nameAr = "جراج", nameEn = "Garage", iconImage = "https://example.com/icons/garage.png"),
            Specification(nameAr = "حديقة", nameEn = "Garden", iconImage = "https://example.com/icons/garden.png"),
            Specification(nameAr = "مكيف", nameEn = "Air Conditioning", iconImage = "https://example.com/icons/ac.png"),
            Specification(nameAr = "أمن", nameEn = "Security", iconImage = "https://example.com/icons/security.png"),
            Specification(nameAr = "مصعد", nameEn = "Elevator", iconImage = "https://example.com/icons/elevator.png")
        )

        specifications.forEach { specRepo.create(it) }
        println("Specifications seeded")
    }

    private suspend fun seedSubscriptionPlans() {
        val planRepo = SubscriptionPlanRepository()

        val plans = listOf(
            SubscriptionPlan(
                image = "https://example.com/plans/basic.png",
                nameAr = "أساسي",
                nameEn = "Basic",
                price = 0.0,
                features = listOf("5 ads per month", "Basic support"),
                suitableAr = "للمستخدمين الجدد",
                suitableEn = "For new users",
                discountValue = 0.0
            ),
            SubscriptionPlan(
                image = "https://example.com/plans/premium.png",
                nameAr = "مميز",
                nameEn = "Premium",
                price = 99.99,
                features = listOf("Unlimited ads", "Featured listings", "Priority support"),
                suitableAr = "للوسطاء العقاريين",
                suitableEn = "For real estate agents",
                discountValue = 10.0
            ),
            SubscriptionPlan(
                image = "https://example.com/plans/enterprise.png",
                nameAr = "الشركات",
                nameEn = "Enterprise",
                price = 299.99,
                features = listOf("Unlimited ads", "API access", "Custom branding", "Dedicated support"),
                suitableAr = "للشركات الكبيرة",
                suitableEn = "For large companies",
                discountValue = 20.0
            )
        )

        plans.forEach { planRepo.create(it) }
        println("Subscription plans seeded")
    }

    private suspend fun seedOnboarding() {
        val onboardingRepo = OnboardingRepository()

        val items = listOf(
            Onboarding(
                image = "https://example.com/onboarding/1.png",
                titleAr = "مرحباً بك",
                titleEn = "Welcome",
                descriptionAr = "اكتشف أفضل العقارات في منطقتك",
                descriptionEn = "Discover the best properties in your area",
                order = 1
            ),
            Onboarding(
                image = "https://example.com/onboarding/2.png",
                titleAr = "ابحث بسهولة",
                titleEn = "Search Easily",
                descriptionAr = "استخدم الفلاتر المتقدمة للعثور على العقار المثالي",
                descriptionEn = "Use advanced filters to find your perfect property",
                order = 2
            ),
            Onboarding(
                image = "https://example.com/onboarding/3.png",
                titleAr = "تواصل مباشر",
                titleEn = "Direct Communication",
                descriptionAr = "تحدث مع مالكي العقارات مباشرة",
                descriptionEn = "Chat directly with property owners",
                order = 3
            )
        )

        items.forEach { onboardingRepo.create(it) }
        println("Onboarding items seeded")
    }

    private suspend fun seedUsers() {
        val userRepo = UserRepository()

        val users = listOf(
            User(
                name = "Ahmed Hassan",
                phone = "+201234567890",
                email = "ahmed@example.com",
                passwordHash = BCrypt.hashpw("password123", BCrypt.gensalt()),
                accountType = Constants.ACCOUNT_TYPE_PERSON,
                status = Constants.USER_STATUS_ACTIVE,
                isPhoneVerified = true,
                isEmailVerified = true,
                address = "New Cairo, Egypt",
                latitude = 30.0444,
                longitude = 31.2357
            ),
            User(
                name = "Real Estate Co.",
                phone = "+201987654321",
                email = "info@realestate.com",
                passwordHash = BCrypt.hashpw("company123", BCrypt.gensalt()),
                accountType = Constants.ACCOUNT_TYPE_COMPANY,
                status = Constants.USER_STATUS_ACTIVE,
                isPhoneVerified = true,
                isEmailVerified = true,
                address = "Downtown Cairo, Egypt",
                latitude = 30.0626,
                longitude = 31.2497
            )
        )

        users.forEach { userRepo.create(it) }
        println("Users seeded")
    }

    private suspend fun seedProperties() {
        val propertyRepo = PropertyRepository()
        val userRepo = UserRepository()
        val countryRepo = CountryRepository()
        val governorateRepo = GovernorateRepository()
        val specRepo = SpecificationRepository()

        val users = userRepo.findAll(1, 10)
        val countries = countryRepo.findAll()
        val governorates = governorateRepo.findAll()
        val specifications = specRepo.findAll()

        if (users.isNotEmpty() && countries.isNotEmpty() && governorates.isNotEmpty()) {
            val properties = listOf(
                Property(
                    images = listOf("https://example.com/properties/1-1.jpg", "https://example.com/properties/1-2.jpg"),
                    title = "Luxury Villa in New Cairo",
                    description = "Beautiful 3-bedroom villa with swimming pool and garden",
                    categoryId = String(), // Would be actual category ID
                    categoryName = "Villa",
                    propertyTypeId = String(), // Would be actual property type ID
                    propertyTypeName = "For Sale",
                    specifications = specifications.take(3).map { it.id },
                    area = 350.0,
                    rooms = 3,
                    baths = 2,
                    price = 2500000.0,
                    locationString = "New Cairo, Egypt",
                    latitude = 30.0444,
                    longitude = 31.2357,
                    ownerId = users.first().id,
                    countryId = countries.first().id,
                    governorateId = governorates.first().id,
                    featured = true
                ),
                Property(
                    images = listOf("https://example.com/properties/2-1.jpg"),
                    title = "Modern Apartment in Zamalek",
                    description = "2-bedroom apartment in the heart of Cairo",
                    categoryId = String(),
                    categoryName = "Apartment",
                    propertyTypeId = String(),
                    propertyTypeName = "For Rent",
                    specifications = specifications.take(2).map { it.id },
                    area = 120.0,
                    rooms = 2,
                    baths = 1,
                    price = 15000.0,
                    locationString = "Zamalek, Cairo",
                    latitude = 30.0626,
                    longitude = 31.2497,
                    ownerId = users.first().id,
                    countryId = countries.first().id,
                    governorateId = governorates.first().id,
                    pinned = true
                )
            )

            properties.forEach { propertyRepo.create(it) }
            println("Properties seeded")
        }
    }
}