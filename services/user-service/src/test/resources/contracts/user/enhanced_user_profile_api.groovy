package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return user profile for authenticated user"
    
    request {
        method GET()
        url "/api/v1/users/profile"
        headers {
            contentType applicationJson()
            header 'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.contract.token'
        }
    }
    
    response {
        status OK()
        headers {
            contentType applicationJson()
            header 'Cache-Control': 'no-cache, no-store, max-age=0, must-revalidate'
            header 'X-Content-Type-Options': 'nosniff'
            header 'X-Frame-Options': 'DENY'
            header 'X-XSS-Protection': '1; mode=block'
        }
        body(
            id: "660e8400-e29b-41d4-a716-446655440001",
            userId: "550e8400-e29b-41d4-a716-446655440000",
            firstName: "Contract",
            lastName: "TestUser",
            email: "contract.test@doordash.com",
            dateOfBirth: "1990-01-01",
            phoneNumber: "+1234567890",
            profilePictureUrl: "https://example.com/profile.jpg",
            bio: "Contract testing user profile",
            city: "San Francisco",
            state: "CA",
            zipCode: "94103",
            createdAt: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}'),
            updatedAt: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')
        )
    }
}

Contract.make {
    description "should create user profile with valid data"
    
    request {
        method POST()
        url "/api/v1/users/profile"
        headers {
            contentType applicationJson()
            header 'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.contract.token'
        }
        body(
            firstName: "John",
            lastName: "Doe",
            dateOfBirth: "1990-01-01",
            phoneNumber: "+1234567890",
            bio: "Contract test bio",
            city: "San Francisco",
            state: "CA",
            zipCode: "94103"
        )
    }
    
    response {
        status CREATED()
        headers {
            contentType applicationJson()
            header 'Location': regex('http://localhost:[0-9]+/api/v1/users/[a-f0-9-]+/profile')
        }
        body(
            id: regex('[a-f0-9-]+'),
            userId: regex('[a-f0-9-]+'),
            firstName: "John",
            lastName: "Doe",
            email: regex('.+@.+\\..+'),
            dateOfBirth: "1990-01-01",
            phoneNumber: "+1234567890",
            bio: "Contract test bio",
            city: "San Francisco",
            state: "CA",
            zipCode: "94103",
            createdAt: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}'),
            updatedAt: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')
        )
    }
}

Contract.make {
    description "should update user profile with valid data"
    
    request {
        method PUT()
        url "/api/v1/users/profile"
        headers {
            contentType applicationJson()
            header 'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.contract.token'
        }
        body(
            firstName: "Jane",
            lastName: "Smith",
            phoneNumber: "+0987654321",
            bio: "Updated contract test bio",
            city: "Los Angeles",
            state: "CA",
            zipCode: "90210"
        )
    }
    
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
            id: regex('[a-f0-9-]+'),
            userId: regex('[a-f0-9-]+'),
            firstName: "Jane",
            lastName: "Smith",
            email: regex('.+@.+\\..+'),
            phoneNumber: "+0987654321",
            bio: "Updated contract test bio",
            city: "Los Angeles",
            state: "CA",
            zipCode: "90210",
            createdAt: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}'),
            updatedAt: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')
        )
    }
}

Contract.make {
    description "should delete user profile"
    
    request {
        method DELETE()
        url "/api/v1/users/profile"
        headers {
            header 'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.contract.token'
        }
    }
    
    response {
        status NO_CONTENT()
        headers {
            header 'Cache-Control': 'no-cache, no-store, max-age=0, must-revalidate'
        }
    }
}

Contract.make {
    description "should return 401 for unauthenticated request"
    
    request {
        method GET()
        url "/api/v1/users/profile"
        headers {
            contentType applicationJson()
        }
    }
    
    response {
        status UNAUTHORIZED()
        headers {
            contentType applicationJson()
        }
        body(
            timestamp: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*'),
            status: 401,
            error: "Unauthorized",
            message: "Authentication required",
            path: "/api/v1/users/profile"
        )
    }
}

Contract.make {
    description "should return 400 for invalid profile creation data"
    
    request {
        method POST()
        url "/api/v1/users/profile"
        headers {
            contentType applicationJson()
            header 'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.contract.token'
        }
        body(
            firstName: "",
            lastName: null,
            dateOfBirth: "2030-01-01",
            phoneNumber: "123",
            state: "INVALID",
            zipCode: "12"
        )
    }
    
    response {
        status BAD_REQUEST()
        headers {
            contentType applicationJson()
        }
        body(
            timestamp: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*'),
            status: 400,
            error: "Bad Request",
            message: "Validation failed",
            path: "/api/v1/users/profile",
            errors: [
                [
                    field: "firstName",
                    rejectedValue: "",
                    message: regex('.+')
                ]
            ]
        )
    }
}

Contract.make {
    description "should return 404 for non-existent user profile"
    
    request {
        method GET()
        url "/api/v1/users/99999999-9999-9999-9999-999999999999/profile"
        headers {
            contentType applicationJson()
            header 'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.contract.token'
        }
    }
    
    response {
        status NOT_FOUND()
        headers {
            contentType applicationJson()
        }
        body(
            timestamp: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*'),
            status: 404,
            error: "Not Found",
            message: "User profile not found",
            path: regex('/api/v1/users/.+/profile')
        )
    }
}

Contract.make {
    description "should search user profiles with pagination"
    
    request {
        method GET()
        url value(consumer(regex('/api/v1/users/search\\?query=.+&page=[0-9]+&size=[0-9]+')),
                  producer('/api/v1/users/search?query=contract&page=0&size=20'))
        headers {
            contentType applicationJson()
            header 'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.contract.token'
        }
    }
    
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
            content: [
                [
                    id: regex('[a-f0-9-]+'),
                    userId: regex('[a-f0-9-]+'),
                    firstName: regex('.+'),
                    lastName: regex('.+'),
                    email: regex('.+@.+\\..+'),
                    city: regex('.+'),
                    state: regex('[A-Z]{2}'),
                    createdAt: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}'),
                    updatedAt: regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')
                ]
            ],
            pageable: [
                pageNumber: 0,
                pageSize: 20,
                sort: [
                    sorted: false,
                    empty: true,
                    unsorted: true
                ],
                offset: 0,
                paged: true,
                unpaged: false
            ],
            totalElements: regex('[0-9]+'),
            totalPages: regex('[0-9]+'),
            last: regex('true|false'),
            first: true,
            numberOfElements: regex('[0-9]+'),
            size: 20,
            number: 0,
            sort: [
                sorted: false,
                empty: true,
                unsorted: true
            ],
            empty: false
        )
    }
}

Contract.make {
    description "should upload profile picture"
    
    request {
        method POST()
        url "/api/v1/users/profile/avatar"
        headers {
            header 'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.contract.token'
            header 'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW'
        }
        multipart(
            file: named(
                name: "file",
                content: "mock-image-content".bytes,
                contentType: "image/jpeg"
            )
        )
    }
    
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
            profilePictureUrl: regex('https?://.+\\.(jpg|jpeg|png|gif)')
        )
    }
}
