/**
 * 
 */
package com.energizer.core.datafeed.processor.customer;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.energizer.core.datafeed.facade.impl.DefaultEnergizerPasswordExpiryFacade;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.services.email.EnergizerGenericEmailGenerationService;


/**
 * @author M1023278
 * 
 */
public class EnergizerPasswordExpiryJob extends AbstractJobPerformable<EnergizerCronJobModel>
{
	private static final Logger LOG = Logger.getLogger(EnergizerPasswordExpiryJob.class);
	@Resource
	EnergizerGenericEmailGenerationService energizerGenericEmailGenerationService;

	@Resource(name = "userService")
	private UserService userService;

	@Resource
	protected FlexibleSearchService flexibleSearchService;

	@Resource
	protected DefaultEnergizerPasswordExpiryFacade defaultEnergizerPasswordExpiryFacade;

	@Resource
	EmailService emailService;

	@Value("${offline.energizer.site}")
	String siteName;

	@Resource(name = "modelService")
	private ModelService modelService;

	@Value("${passwordExpiryDays}")
	int passwordExpiryDays;

	@Value("${passwordNotificationDays}")
	int passwordNotificationDays;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.CronJobModel
	 * )
	 */
	@Override
	public PerformResult perform(final EnergizerCronJobModel arg0)
	{

		final List<EnergizerB2BCustomerModel> energizerB2BCustomerModels = defaultEnergizerPasswordExpiryFacade
				.getEnergizerCustomers();

		System.out.println("List Size :" + energizerB2BCustomerModels.size());
		System.out.println("energizerB2BCustomerModels list of custome" + energizerB2BCustomerModels.get(0).getEmail());
		for (final EnergizerB2BCustomerModel energizerB2BCustomerModel : energizerB2BCustomerModels)
		{
			final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
			Date maxDate;
			try
			{
				maxDate = sdf.parse("20-May-2015");
				final String passwordModifiedTime = sdf.format(maxDate);
				//Date latestModifiedTime = energizerB2BCustomerModel.getPasswordModifiedTime();

				final Date latestModifiedTime = sdf.parse(passwordModifiedTime);
				final Calendar calPasswordModifiedDate = Calendar.getInstance();
				calPasswordModifiedDate.setTime(latestModifiedTime);

				LOG.debug("Calender password modified date is " + calPasswordModifiedDate.getTime());
				calPasswordModifiedDate.add(Calendar.DATE, passwordExpiryDays);

				LOG.debug("Calender::: Pasword Expiry Date " + calPasswordModifiedDate.getTime());
				final Calendar calNotificationDate = Calendar.getInstance();


				if (calNotificationDate.compareTo(calPasswordModifiedDate) <= 0)
				{
					calNotificationDate.add(Calendar.DATE, passwordNotificationDays);

					LOG.debug("Calender Notification date " + calNotificationDate.getTime());
					if (calNotificationDate.compareTo(calPasswordModifiedDate) <= 0)
					{

						LOG.debug("Dear " + energizerB2BCustomerModel.getName() + " Your password has not been expired");
					}
					else
					{
						final Calendar calCurrentDate = Calendar.getInstance();
						final int remainingDays = calPasswordModifiedDate.getTime().getDate() - calCurrentDate.getTime().getDate();
						final long diff = calPasswordModifiedDate.getTimeInMillis() - calCurrentDate.getTimeInMillis();

						//final int diffInDays = (int) (diff / (1000 * 60 * 60 * 24));

						LOG.debug("Your Password will expire in " + remainingDays);
						prepareEmail(energizerB2BCustomerModel, remainingDays);


					}
				}
				else
				{

					LOG.debug("Dear " + energizerB2BCustomerModel.getName() + " Your password has not been expired");
					prepareEmail(energizerB2BCustomerModel, 0);
				}
			}
			catch (final ParseException e)
			{

				LOG.error("Exception occured in EnergizerPasswordExpiryJob" + e.getMessage());
			}




		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	public void prepareEmail(final EnergizerB2BCustomerModel energizerB2BCustomerModel, final int days)
	{
		final Map<String, Object> contextmap = new HashMap<String, Object>();


		final List<CMSSiteModel> cmsSiteModels = defaultEnergizerPasswordExpiryFacade.getCMSSiteByName(siteName);


		LOG.debug("basesite " + cmsSiteModels.get(0).getName());
		contextmap.put("baseSite", cmsSiteModels.get(0));
		final EmailAddressModel fromEmail = getEmailService().getOrCreateEmailAddressForEmail("ehpvalidation@gmail.com",
				"ehpvalidation");
		final EmailAddressModel toEmail = getEmailService().getOrCreateEmailAddressForEmail(energizerB2BCustomerModel.getEmail(),
				"EnergizerCustomer");
		final List<EmailAddressModel> toAddress = new ArrayList<EmailAddressModel>();
		toAddress.add(toEmail);
		final List<EmailAddressModel> ccAddress = new ArrayList<EmailAddressModel>();
		ccAddress.add(fromEmail);
		contextmap.put("customer", energizerB2BCustomerModel);
		contextmap.put("expiryDays", days);

		contextmap.put("displayName", energizerB2BCustomerModel.getDisplayName());
		if (days > 0)
		{
			energizerGenericEmailGenerationService.generateAndSendEmail("EnergizerPasswordExpiryEmailTemplate", toAddress,
					fromEmail, ccAddress, energizerB2BCustomerModel.getSessionLanguage(), contextmap);
		}
		else
		{
			energizerGenericEmailGenerationService.generateAndSendEmail("EnergizerPasswordExpiredEmailTemplate", toAddress,
					fromEmail, ccAddress, energizerB2BCustomerModel.getSessionLanguage(), contextmap);
		}

	}

	/**
	 * @return the emailService
	 */
	public EmailService getEmailService()
	{
		return emailService;
	}

	/**
	 * @param emailService
	 *           the emailService to set
	 */
	public void setEmailService(final EmailService emailService)
	{
		this.emailService = emailService;
	}



}