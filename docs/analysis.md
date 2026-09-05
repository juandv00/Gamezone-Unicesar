# Analysis — GameZone Unicesar

This document contains the team's answers to the eleven guiding questions
required before designing the class diagrams for the GameZone Unicesar system.

## About the people in the system

### 1. What attributes are common to all people who interact with the store, and which are specific to each type of person? How is this distinction reflected in a class hierarchy?

All people who interact with the store share three basic attributes: `name`,
`id` (identification number), and `phone` (contact phone number). These
attributes do not depend on the role the person plays in the business.

Beyond these common attributes, each specific role has its own particular
attributes:
- **Client**: `email` and purchase history (resolved as a query over the
  sales already registered, rather than a stored attribute — see question
  regarding relationships below).
- **Seller**: `employeeCode` and `shift`.

This distinction is reflected through **generalization/specialization**: we
create an abstract base class `Person` that holds the three common
attributes, and two concrete subclasses, `Client` and `Seller`, that inherit
from `Person` and add their own particular attributes. This avoids
duplicating the common attributes in both subclasses and models the real
business relationship: every `Client` and every `Seller` "is a" `Person`.

### 2. Should there be a class that represents a "generic person" without specifying their role? Why or why not? What implication does this decision have on the possibility of instantiating that class?

No, there should not be an instantiable "generic person" class. In the real
business context described, no person interacts with the store without a
defined role — every person is either a `Client` or a `Seller`. Allowing a
generic `Person` object to be created would not correspond to any real
business need and would violate the intent of the model.

For this reason, `Person` must be declared as an **abstract class**. In
Java, the `abstract` keyword is precisely the mechanism that prevents direct
instantiation (`new Person()` would not compile), forcing the system to only
create objects of the concrete subclasses `Client` or `Seller`. This
guarantees that every `Person` object in memory always has a well-defined
role.

## About the products in the system

### 3. What characteristics do all products marketed by the store have in common? Which characteristics are specific to each type of product?

All products share four basic attributes: `id`, `title`, `price`, and
`stock` (available quantity in inventory), regardless of their specific
type.

Beyond these, each product type has its own particular attributes:
- **VideoGame**: `platform`, `genre`, and `ageRating`.
- **Console**: `brand`, `model`, and `generation`.

As with people, this is modeled through generalization/specialization: an
abstract base class `Product` holds the common attributes, and two concrete
subclasses, `VideoGame` and `Console`, inherit from it and add their
particular attributes.

### 4. Each type of product must be able to present a description that integrates its particular characteristics. How should this behavior be declared in the base class to guarantee that all subclasses implement it in their own way? What object-oriented mechanism allows this?

The base class `Product` cannot implement a generic description method
itself, because it has no knowledge of the particular attributes that belong
to its subclasses (it does not know about `platform`, `brand`, etc.).
However, the design must guarantee that every subclass provides its own
implementation.

The solution is to declare an **abstract method** in the base class:

```java
public abstract String getDescription();
```

This method has no body in `Product`, and being marked `abstract` forces
every concrete subclass to implement it, or the code will not compile. Each
subclass then builds its own description using its particular attributes.

The object-oriented mechanism that enables this is **polymorphism**,
supported by **method overriding**: the same message (`getDescription()`)
produces different behavior depending on the actual type of the object that
receives it. In Java, subclasses must mark their implementation with the
`@Override` annotation, as required by the code restrictions of this
workshop.

## About sales and relationships between entities

### 5. A sale involves a client, a seller, and one or more products. What type of relationships exist between the class that represents the sale and the other classes of the system? Are these relationships of inheritance, association, composition, or another type? Justify.

There is no inheritance relationship here — a `Sale` is not a `Client`, nor
is it a `Product`. The relationships present are of two kinds:

- **`Sale` to `Client` and `Sale` to `Seller`: simple association.** A sale
  references one client and one seller, but neither of them depends on the
  sale to exist. If a sale were deleted, the client and seller would
  continue to exist normally, since they existed before the sale and
  continue to exist independently of it.

- **`Sale` to `Product`: aggregation.** A sale contains a collection of
  products, but those products have their own lifecycle independent of the
  sale — they exist in the store's inventory both before and after being
  sold. This rules out composition, since composition would imply that the
  product cannot exist without the sale, which is false. Aggregation
  correctly represents a "has-a" relationship where the parts have
  independent existence.

The multiplicity is: one `Sale` references exactly one `Client` and one
`Seller`; one `Sale` aggregates at least one `Product` (1..*), enforcing the
business rule that a sale must contain at least one product.

### 6. Should the sale be responsible for calculating its own total, or should this responsibility fall on another class? Argue your decision.

The sale (`Sale`) should be responsible for calculating its own total. This
follows the **Information Expert** principle: the responsibility for a
calculation should be assigned to the class that has the information needed
to perform it. `Sale` is the class that holds the complete list of products
that compose it, making it the "expert" on that data.

If this responsibility were delegated to another class (for example, a
service class), that class would need to access the internal list of
products stored inside `Sale`, breaking encapsulation — especially since all
domain attributes must be declared as `private`. The service layer
(`SaleService`) still has an important role: it enforces business rules
(minimum one product, stock validation, inventory update), but the
arithmetic calculation of the total belongs to the `Sale` object itself,
through a method such as `calculateTotal()`.

## About business constraints

### 7. How is it guaranteed in the design that a sale cannot be registered without at least one product? At what point in the system should this rule be validated?

This rule is a business rule, and business rules belong to the **service
layer**, according to the layered architecture defined for this system. The
validation should be implemented in `SaleService`, specifically in the
method responsible for registering a new sale (e.g., `registerSale(...)`).

Before creating the `Sale` object and persisting it, `SaleService` must
verify that the list of products provided is neither null nor empty. If the
list does not contain at least one product, the operation must be rejected
and no sale should be created or persisted.

This does not prevent the `Sale` class itself from having a basic structural
safeguard in its constructor (defense in depth), but the decision of whether
the sale operation as a whole succeeds or fails belongs to the service
layer, since it is the layer responsible for orchestrating the complete
operation.

### 8. How is the automatic inventory update reflected in the design when a sale is registered? Which classes are involved in this operation?

This behavior involves three coordinated classes, orchestrated by the
service layer:

1. **`SaleService`**: receives the request to register a sale (client,
   seller, list of products with quantities). For each product, it verifies
   that there is enough stock available. If the available stock is
   insufficient for any product, the entire operation is rejected — partial
   sales are not allowed. If all validations pass, it coordinates the stock
   reduction, creates the `Sale` object, calculates its total, and persists
   it.

2. **`ProductService`**: exposes the method that actually modifies the
   stock of a product (for example, `reduceStock(int quantity)`), since it
   is responsible for the business rules of the product module, such as
   preventing negative stock. `SaleService` depends on `ProductService` to
   perform this operation, representing a collaboration between two service
   classes.

3. **`Product`**: internally holds the private `stock` attribute and may
   expose a method such as `decreaseStock(int amount)` that protects its own
   internal state.

The user interface layer is never directly involved in this operation — it
only collects input and delegates everything to `SaleService`, respecting
the mandatory dependency direction `ui → service → persistence → model`.

## About the layered organization

### 9. The system must be organized into four layers: model, persistence, services, and user interface. What type of classes belong to each layer? What criterion determines in which layer a class should be placed?

The criterion that determines a class's layer is **the responsibility that
class fulfills**. Each layer has a single, well-defined responsibility, and
a class belongs to the layer whose responsibility matches what that class
actually does:

- **model**: `Person`, `Client`, `Seller`, `Product`, `VideoGame`,
  `Console`, `Sale`. These classes represent business concepts, with their
  own attributes and behaviors related purely to the domain. They know
  nothing about files, menus, or external validation logic.

- **persistence**: `ProductPersistence`, `PersonPersistence`,
  `SalePersistence`. These classes are solely responsible for reading and
  writing data to and from files, converting domain objects into persisted
  data and back.

- **service**: `ProductService`, `PersonService`, `SaleService`. These
  classes orchestrate operations and enforce business rules, coordinating
  between the model and the persistence layer.

- **ui**: the console menu class (`ConsoleUI`). This is the only layer that
  interacts directly with the user, without containing business logic or
  accessing files directly.

The `Main` class sits at the root package and is only responsible for
starting the application and wiring the dependencies together.

### 10. Why should the logic for saving and retrieving data from files not be inside the domain classes? What problems arise when these responsibilities are mixed?

This follows the **Single Responsibility Principle**: a domain class such as
`Product` should have only one reason to change — a change in the business
rules that define what a product is. If file-handling logic were added
inside it, the class would gain a second, unrelated reason to change: a
change in the storage format (for example, moving from plain text to CSV).

Mixing these responsibilities creates several concrete problems:

- **Unnecessary coupling**: the domain class would depend on technical
  details (file paths, formats, I/O exception handling) that have nothing to
  do with the business itself.
- **Harder maintenance**: changing the storage mechanism would require
  modifying every domain class, instead of only the corresponding
  persistence class.
- **Harder testing**: testing the business logic of a domain class would
  require simulating file access, when it should be testable in isolation.
- It directly violates the architecture defined for this system, which
  explicitly forbids model classes from containing file-access logic.

### 11. What dependencies are allowed between the layers, and which are forbidden? Justify the meaning of the allowed dependencies.

The allowed dependencies follow a single direction:


Specifically:
- `ui` depends on `service`.
- `service` depends on both `model` and `persistence`.
- `persistence` depends on `model`.
- `model` does not depend on any other layer.

The following dependencies are forbidden:
- `ui` depending directly on `persistence`, bypassing `service`.
- `model` depending on `service`, `persistence`, or `ui`.
- `persistence` depending on `service` or `ui`.

This direction of dependency follows the principle that low-level, stable
layers should not depend on high-level, volatile ones. The `model` layer
represents the core business rules in their purest form, and it must be able
to exist and compile without any knowledge of how it is persisted,
displayed, or externally validated. This allows the system to change its
user interface or its persistence mechanism without touching the domain
model at all. The `service` layer acts as the only layer that knows both the
model and the persistence layer, and it is the only entry point available to
the `ui` layer, centralizing business rule enforcement in a single place.