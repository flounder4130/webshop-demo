# Webshop Demo

This is a sample project for the following debugging tutorial: https://flounder.dev/posts/your-programs-are-not-single-threaded/

## Running the Application

```bash
./mvnw spring-boot:run
```

## API Endpoints

### Products
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get a product by ID

### Cart
- `GET /api/cart` - View cart contents
- `POST /api/cart/add/{productId}?quantity=1` - Add product to cart
- `DELETE /api/cart` - Clear cart

### Orders
- `POST /api/orders/checkout?customerName=John` - Place an order
- `GET /api/orders/invoices` - List all invoices

## Example Usage

```bash
# List products
curl http://localhost:8080/api/products

# Add to cart
curl -X POST "http://localhost:8080/api/cart/add/1?quantity=2"

# View cart
curl http://localhost:8080/api/cart

# Checkout
curl -X POST "http://localhost:8080/api/orders/checkout?customerName=John"

# View invoices
curl http://localhost:8080/api/orders/invoices
```
