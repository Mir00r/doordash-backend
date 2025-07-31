package contracts.user

import org.springframework.cloud.contract.spec.Contract

/**
 * Contract definition for User Profile API.
 * 
 * This contract ensures that the User Service API
 * maintains compatibility with consuming services.
 */

Contract.make {
    description "Should return user profile for valid user ID"
    
    request {
        method GET()
        url "/api/v1/users/123e4567-e89b-12d3-a456-426614174000/profile"
        headers {
            accept applicationJson()
            header("Authorization", "Bearer valid-jwt-token")
        }
    }
    
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            id: "123e4567-e89b-12d3-a456-426614174000",
            firstName: "John",
            lastName: "Doe",
            email: "john.doe@doordash.com",
            phoneNumber: "+1234567890",
            dateOfBirth: "1990-01-01"
        ])
    }
}

Contract.make {
    description "Should return 404 for non-existent user"
    
    request {
        method GET()
        url "/api/v1/users/00000000-0000-0000-0000-000000000000/profile"
        headers {
            accept applicationJson()
            header("Authorization", "Bearer valid-jwt-token")
        }
    }
    
    response {
        status NOT_FOUND()
        headers {
            contentType applicationJson()
        }
        body([
            error: "User not found",
            message: "User not found"
        ])
    }
}

Contract.make {
    description "Should create user profile successfully"
    
    request {
        method POST()
        url "/api/v1/users/123e4567-e89b-12d3-a456-426614174000/profile"
        headers {
            contentType applicationJson()
            accept applicationJson()
            header("Authorization", "Bearer valid-jwt-token")
        }
        body([
            firstName: "John",
            lastName: "Doe",
            dateOfBirth: "1990-01-01",
            phoneNumber: "+1234567890"
        ])
    }
    
    response {
        status CREATED()
        headers {
            contentType applicationJson()
        }
        body([
            id: anyUuid(),
            firstName: "John",
            lastName: "Doe",
            email: "john.doe@doordash.com",
            phoneNumber: "+1234567890",
            dateOfBirth: "1990-01-01"
        ])
    }
}

Contract.make {
    description "Should update user profile successfully"
    
    request {
        method PUT()
        url "/api/v1/users/123e4567-e89b-12d3-a456-426614174000/profile"
        headers {
            contentType applicationJson()
            accept applicationJson()
            header("Authorization", "Bearer valid-jwt-token")
        }
        body([
            firstName: "Jane",
            lastName: "Smith",
            phoneNumber: "+0987654321"
        ])
    }
    
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            id: "123e4567-e89b-12d3-a456-426614174000",
            firstName: "Jane",
            lastName: "Smith",
            email: "john.doe@doordash.com",
            phoneNumber: "+0987654321",
            dateOfBirth: "1990-01-01"
        ])
    }
}

Contract.make {
    description "Should search users by name"
    
    request {
        method GET()
        url "/api/v1/users/search"
        headers {
            accept applicationJson()
            header("Authorization", "Bearer valid-jwt-token")
        }
        queryParameters {
            parameter("name", "John")
        }
    }
    
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            [
                id: "123e4567-e89b-12d3-a456-426614174000",
                firstName: "John",
                lastName: "Doe",
                email: "john.doe@doordash.com",
                phoneNumber: "+1234567890",
                dateOfBirth: "1990-01-01"
            ]
        ])
    }
}

Contract.make {
    description "Should return empty array for no search results"
    
    request {
        method GET()
        url "/api/v1/users/search"
        headers {
            accept applicationJson()
            header("Authorization", "Bearer valid-jwt-token")
        }
        queryParameters {
            parameter("name", "NonExistent")
        }
    }
    
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([])
    }
}
