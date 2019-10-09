package es.us.isa.jsonmutator.mutator;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

import static es.us.isa.jsonmutator.util.JsonManager.insertElement;

/**
 * Superclass for mutators. A mutator decides on the type of mutation to be applied
 * to an element (string, int, etc.) based on the different mutation operators
 * enabled for that element (boundary, replacement, etc.).
 *
 * @author Alberto Martin-Lopez
 */
public abstract class AbstractMutator extends RandomManager {

    protected HashMap<String, AbstractOperator> operators; // Map of mutation operators. The key should be equal to the one in the properties file
    protected float prob; // Probability to apply this mutation to an element. Configured in properties file

    public AbstractMutator() {
        super();
        operators = new HashMap<>();
    }

    public boolean shouldApplyMutation() {
        return rand2.nextFloat() <= prob;
    }

    /**
     * Given a list (map) of mutation operators, selects one. To do so, it generates
     * a random float between 0 and {@code sum(weights_of_operators)} and chooses the
     * operator whose weight is in that range. Example: <br>
     * 1.- StringReplacement. {@code weight=0.1}. <br>
     * 2.- StringMutation. {@code weight=0.4}. <br>
     * 3.- StringBoundary. {@code weight=0.2}. <br>
     * - {@code randomFloat=0.367} => StringMutation is selected, because the float
     * falls in the range 0.1-0.5 (0.1+0.4)
     *
     * @return The name of the mutation operator selected, or null if the map doesn't
     * contain any operator
     */
    public String getOperator() {
        float sumWeights = operators.values().stream() // Sum all weights
                .map(AbstractOperator::getWeight)
                .reduce(0f, Float::sum);
        float randomFloat = rand2.nextFloat() * sumWeights; // Generate random float between 0 and sumWeights

        float acc = 0;
        String operatorName = null;
        Iterator<Map.Entry<String,AbstractOperator>> operatorIterator = operators.entrySet().iterator();
        Map.Entry<String,AbstractOperator> currentOperator;
        while (operatorIterator.hasNext() && sumWeights != 0) { // Iterate over mutation operators if sum of weights is greater than 0
            currentOperator = operatorIterator.next();
            acc += currentOperator.getValue().getWeight();
            if (randomFloat <= acc) { // When the condition is met, get operator name and break loop
                operatorName = currentOperator.getKey();
                break;
            }
        }

        return operatorName;
    }

    /**
     * Given an object and the name of a property, mutate the value of that property
     * with probability {@link AbstractMutator#prob}
     *
     * @return True if the mutation was applied, false otherwise
     */
    public boolean mutate(ObjectNode objectNode, String propertyName) {
        if (shouldApplyMutation()) {
            Object propertyValue;
            if (objectNode.get(propertyName).isIntegralNumber()) {
                propertyValue = objectNode.get(propertyName).asLong(); // Get number to mutate
            } else if (objectNode.get(propertyName).isFloatingPointNumber()) {
                propertyValue = objectNode.get(propertyName).asDouble(); // Get floating number to mutate
            } else if (objectNode.get(propertyName).isTextual()) {
                propertyValue = objectNode.get(propertyName).asText(); // Get string to mutate
            } else if (objectNode.get(propertyName).isBoolean()) {
                propertyValue = objectNode.get(propertyName).asBoolean(); // Get boolean to mutate
            } else if (objectNode.get(propertyName).isObject() || objectNode.get(propertyName).isArray()) {
                propertyValue = objectNode.get(propertyName); // Get object or array to mutate
            } else {
                throw new IllegalArgumentException("The value of the property '" + propertyName +
                        "' cannot be mutated. Allowed mutations: strings, ints, floats, booleans, " +
                        "objects or arrays.");
            }

            // Mutate element by randomly choosing one mutation operator among 'operators' and applying the mutation:
            String operator = getOperator();
            if (operator != null) {
                Object mutatedElement = operators.get(operator).mutate(propertyValue);
                insertElement(objectNode, propertyName, mutatedElement); // Replace original element with mutated element
                return true;
            }
        }
        return false;
    }

    /**
     * Given an array and the index of an element, mutate that element
     * with probability {@link AbstractMutator#prob}
     *
     * @return True if the mutation was applied, false otherwise
     */
    public boolean mutate(ArrayNode arrayNode, int index) {
        if (shouldApplyMutation()) {
            Object arrayElement;
            if (arrayNode.get(index).isIntegralNumber()) {
                arrayElement = arrayNode.get(index).asLong(); // Get number to mutate
            } else if (arrayNode.get(index).isFloatingPointNumber()) {
                arrayElement = arrayNode.get(index).asDouble(); // Get floating number to mutate
            } else if (arrayNode.get(index).isTextual()) {
                arrayElement = arrayNode.get(index).asText(); // Get string to mutate
            } else if (arrayNode.get(index).isBoolean()) {
                arrayElement = arrayNode.get(index).asBoolean(); // Get boolean to mutate
            } else if (arrayNode.get(index).isObject() || arrayNode.get(index).isArray()) {
                arrayElement = arrayNode.get(index); // Get object or array to mutate
            } else {
                throw new IllegalArgumentException("The element at index position " + index +
                        " cannot be mutated. Allowed mutations: strings, ints, floats, booleans, " +
                        "objects or arrays.");
            }

            // Mutate element by randomly choosing one mutation operator among 'operators' and applying the mutation:
            String operator = getOperator();
            if (operator != null) {
                Object mutatedElement = operators.get(operator).mutate(arrayElement);
                insertElement(arrayNode, index, mutatedElement); // Replace original element with mutated element
                return true;
            }
        }
        return false;
    }
}
