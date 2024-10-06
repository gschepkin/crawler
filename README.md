# crawler
Crawler is an application to get titles from multiple urls.

## Libraries
- `cats cats-effect`
- `http4s`
- `tapir`
- `doobie`
- `circe`
- `flyway`
- `pureconfig`
- `scraper`

## To start application with docker
Use `docker-compose up`

## To start in dev mode
Use `docker-compose -f docker-compose.dev.yml up`

And start Application from src

## Configuration
Everything can be configured from config files or env:
```hocon
# APP
APP_VERSION=0.0.1-SNAPSHOT # need to specify the version for the application and cache

# Client (sttp):
CLIENT_TIMEOUT_PER_REQUEST=10 seconds # timeout to get result by url

# Crawl:
CRAWL_MAX_POOL_REQUESTS=10 # maximum number of requests to send by url in parallel

# Database:
POSTGRES_HOST=0.0.0.0     # db host
POSTGRES_PORT=5445        # db port
POSTGRES_DB=crawler       # database name
POSTGRES_USERNAME=crawler # credentials
POSTGRES_PASSWORD=crawler # credentials
POSTGRES_MAX_POOL_SIZE=10 # max pool of connections to postgresql

# Security
ACCESS_TOKEN=qwerty # token to access to crawl endpoint

# Server
SERVER_HOST=0.0.0.0     # host of application server
SERVER_PORT=3030        # port of application server
SWAGGER_ENABLED=true    # enable swagger endpoint
SWAGGER_TITLE=API       # title of swagger web service
SWAGGER_ENDPOINT=admin  # endpoint of swagger service
SWAGGER_API_VERSION=1.0 # version of api (UI)

# Cache:
CACHE_ENABLED=true                 # to enable caching
CACHE_SUCCESS_URL_TTL=10 minutes   # cache - time to live urls with success response
CACHE_FAILURE_URL_TTL=5 minute     # cache - time to live urls with failure response
CACHE_CLEANER_AWAKE_EVERY=1 minute # cache - cleaner delete old raws from cache (ttl < NOW())
```

## API
### Endpoints:
- `GET /admin` - swagger endpoint by default (to open UI and send requests)
- `GET /health` - liveness probe
- `GET /crawl` - crawl urls to get titles

### Example of request:
```url
GET 
/crawl?url=http://localhost&url=http://localhost&url=http://localhost
```

### Example of success response:
```json
{
    "data": [
        {
            "result": {
                "url": "https://vk.com",
                "title": "VK.com | VK",
                "status": "success"
            },
            "source": "load_by_url"
        },
        {
            "result": {
                "url": "https://google.com",
                "title": "Google",
                "status": "success"
            },
            "source": "load_from_cache"
        },
        {
            "result": {
                "url": "https://ya.ru",
                "title": "Яндекс — быстрый поиск в интернете",
                "status": "success"
            },
            "source": "load_by_url"
        },
        {
            "result": {
                "url": "https://wikipedia.com",
                "title": "Wikipedia",
                "status": "success"
            },
            "source": "load_from_cache"
        },
        {
            "result": {
                "url": "https://test-invalid-url.io",
                "error": "Exception when sending request: GET https://test-invalid-url.io",
                "status": "failure"
            },
            "source": "load_by_url"
        },
        {
            "result": {
                "url": "http://localhost:3030/health",
                "error": "Title not found",
                "status": "failure"
            },
            "source": "load_by_url"
        }
    ],
    "error": null
}
```

### Example of failure response:
```json
{
    "data": null,
    "error": "Access Denied, you need to use correct access token"
}
```