#Using REDIS for managing test groups and variants

# Adding 3 tests (testA, testB and testC)
sadd testgroups testA
sadd testgroups testB
sadd testgroups testC

# Adding variants and weights for each test
hset testA variantA 10
hset testA variantB 20
hset testA variantC 70

hset testB bannerA 70
hset testB bannerB 30

hset testC experienceA 10
hset testC controlExperience 90

# Reading out all testgroups
smembers testgroups

# Reading out all variants
hkeys testA

# Reading out all weights
hvals testA


Response of the API:
{
	variantB,
	bannerA,
	controlExperience
}
