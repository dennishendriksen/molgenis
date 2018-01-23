package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

import static java.lang.String.format;

public abstract class EntityValidator<E extends Entity> implements SmartValidator
{
	private final Class<E> entityClass;

	protected EntityValidator(Class<E> entityClass)
	{
		this.entityClass = entityClass;
	}

	@Override
	public boolean supports(Class<?> clazz)
	{
		return entityClass.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors)
	{
		validate(target, errors, new Object[0]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object target, Errors errors, Object... validationHints)
	{
		if (!(entityClass.equals(target.getClass())))
		{
			throw new IllegalArgumentException(
					format("target is of type '%s' instead of '%s'", target.getClass().getName(),
							entityClass.getName()));
		}

		validateEntity((E) target, errors);
	}

	public EntityValidationErrors<E> validate(E entity)
	{
		EntityValidationErrors<E> entityValidationErrors = new EntityValidationErrors<>(entity);
		validate(entity, entityValidationErrors);
		return entityValidationErrors;
	}

	public abstract void validateEntity(E entity, Errors errors);
}
