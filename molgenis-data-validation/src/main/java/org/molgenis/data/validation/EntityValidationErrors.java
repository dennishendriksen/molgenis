package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.springframework.validation.AbstractErrors;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public class EntityValidationErrors<E extends Entity> extends AbstractErrors
{
	private final transient E entity;
	private List<ObjectError> objectErrors;
	private List<FieldError> fieldErrors;

	public EntityValidationErrors(E entity)
	{
		this.entity = requireNonNull(entity);
		this.objectErrors = new ArrayList<>(1);
		this.fieldErrors = new ArrayList<>(1);
	}

	public E getEntity()
	{
		return entity;
	}

	@Override
	public String getObjectName()
	{
		Object idValue = entity.getIdValue();
		return idValue != null ? idValue.toString() : null;
	}

	@Override
	public void reject(String errorCode, Object[] errorArgs, String defaultMessage)
	{
		String[] codes = { errorCode };
		ObjectError objectError = new ObjectError(getObjectName(), codes, errorArgs, defaultMessage);
		objectErrors.add(objectError);
	}

	@Override
	public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage)
	{
		String[] codes = { errorCode };
		Object rejectedValue = entity.get(field);
		FieldError fieldError = new FieldError(getObjectName(), field, rejectedValue, false, codes, errorArgs,
				defaultMessage);
		fieldErrors.add(fieldError);
	}

	@Override
	public void addAllErrors(Errors errors)
	{
		if (errors.getGlobalErrorCount() > 0)
		{
			objectErrors.addAll(errors.getGlobalErrors());
		}

		if (errors.getFieldErrorCount() > 0)
		{
			getFieldErrors().addAll(errors.getFieldErrors());
		}
	}

	@Override
	public List<ObjectError> getGlobalErrors()
	{
		return unmodifiableList(objectErrors);
	}

	@Override
	public List<FieldError> getFieldErrors()
	{
		return unmodifiableList(fieldErrors);
	}

	@Override
	public Object getFieldValue(String field)
	{
		return entity.get(field); // TODO does this return the correct types?
	}
}
