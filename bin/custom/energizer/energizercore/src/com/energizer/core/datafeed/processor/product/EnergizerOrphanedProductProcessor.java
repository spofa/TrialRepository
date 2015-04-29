/**
 * 
 */
package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerProductService;


/**
 * 
 * This processors imports the orphan product.
 * 
 */
public class EnergizerOrphanedProductProcessor extends AbstractJobPerformable<EnergizerCronJobModel>
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.CronJobModel
	 * )
	 */
	@Resource
	private EnergizerProductService energizerProductService;

	@Resource
	private EmailService emailService;

	private static final Logger LOG = Logger.getLogger(EnergizerOrphanedProductProcessor.class);

	@Override
	public PerformResult perform(final EnergizerCronJobModel arg0)
	{
		try
		{
			//get all products where category is null

			final List<EnergizerProductModel> orphProductList = energizerProductService.getEnergizerOrphanedProductList();
			if (orphProductList != null && !orphProductList.isEmpty())
			{
				final StringBuilder sbProducts = new StringBuilder();
				for (final EnergizerProductModel product : orphProductList)
				{
					LOG.info("PRODUCT WITH NO CATEGORY : " + product.getCode());
					sbProducts.append(product.getCode()).append(", ");
				}
				sendEmail(sbProducts.toString(), arg0.getEmailAddress());
			}
		}
		catch (final Exception e)
		{
			LOG.error(" Error while processing orphaned product list ", e);
		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	private void sendEmail(final String strProducts, final String toEmail)
	{

		try
		{
			final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(toEmail, "Hybris Test Mail");

			final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, toAddress, "",
					"Orphaned Products", "List of Energizer products orphaned : " + strProducts + "\n", null);
			emailService.send(message);
		}
		catch (final Exception e)
		{
			LOG.info("Printing error in ");
			e.printStackTrace();
		}

	}
}
