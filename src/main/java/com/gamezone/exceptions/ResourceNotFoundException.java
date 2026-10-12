package com.gamezone.exceptions;

/**
 * Thrown when a resource (a product, client, seller, sale, among others) is
 * searched by its identifier and does not exist in the system.
 */
public class ResourceNotFoundException extends GameZoneException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    private final String resourceType;
    private final String resourceId;

    /**
     * Creates a new exception for the given resource type and identifier,
     * building a descriptive message in Spanish, for example
     * "Producto con identificador P-01 no existe en el sistema."
     *
     * @param resourceType the type of resource that was searched, in Spanish
     *                     (for example "Producto", "Cliente", "Venta")
     * @param resourceId   the identifier that was searched
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " con identificador " + resourceId + " no existe en el sistema.", ERROR_CODE);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Returns the type of resource that was not found.
     *
     * @return the resource type
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Returns the identifier that was searched.
     *
     * @return the resource identifier
     */
    public String getResourceId() {
        return resourceId;
    }
}
