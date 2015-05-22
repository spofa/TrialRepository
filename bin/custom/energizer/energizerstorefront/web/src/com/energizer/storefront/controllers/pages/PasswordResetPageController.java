/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *  
 */
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.util.Config;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.ForgottenPwdForm;
import com.energizer.storefront.forms.UpdatePwdForm;


/**
 * Controller for the forgotten password pages. Supports requesting a password reset email as well as changing the
 * password once you have got the token that was sent via email.
 */
@Controller
@Scope("tenant")
@RequestMapping(value = "/login/pw")
public class PasswordResetPageController extends AbstractPageController
{
	private static final Logger LOG = Logger.getLogger(PasswordResetPageController.class);

	private static final String REDIRECT_LOGIN = "redirect:/login";
	private static final String REDIRECT_HOME = "redirect:/";

	private static final String UPDATE_PWD_CMS_PAGE = "updatePassword";
	private static final String FORGOTTEN_PASSWORD_EXP_VALUE = "forgottenPassword.emailContext.expiresInMinutes";

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	@RequestMapping(value = "/request", method = RequestMethod.GET)
	public String getPasswordRequest(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute(new ForgottenPwdForm());

		return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPopup;
	}

	@RequestMapping(value = "/request", method = RequestMethod.POST)
	public String passwordRequest(@Valid final ForgottenPwdForm form, final BindingResult bindingResult)
			throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPopup;
		}
		else
		{
			try
			{
				getCustomerFacade().forgottenPassword(form.getEmail());
			}
			catch (final UnknownIdentifierException unknownIdentifierException)
			{
				LOG.warn("Email: " + form.getEmail() + " does not exist in the database.");
			}
			return ControllerConstants.Views.Fragments.Password.ForgotPasswordValidationMessage;
		}
	}

	@RequestMapping(value = "/request-page", method = RequestMethod.GET)
	public String getPasswordRequestPage(final Model model, @RequestParam(required = false) final String uid)
			throws CMSItemNotFoundException
	{
		final ForgottenPwdForm forgottenPwdForm = new ForgottenPwdForm();
		if (null != uid)
		{
			forgottenPwdForm.setEmail(uid);
		}
		model.addAttribute(forgottenPwdForm);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));
		return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPage;
	}

	@RequestMapping(value = "/request-page", method = RequestMethod.POST)
	public String passwordRequestPage(@Valid final ForgottenPwdForm forgottenPwdForm, final BindingResult bindingResult,
			final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute(forgottenPwdForm);
		final String forgottenPassExpValue = Config.getParameter(FORGOTTEN_PASSWORD_EXP_VALUE);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));
		if (bindingResult.hasErrors())
		{
			GlobalMessages.addErrorMessage(model, "form.global.error");
			return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPage;
		}
		else
		{
			try
			{
				getCustomerFacade().forgottenPassword(forgottenPwdForm.getEmail());
				GlobalMessages.addSingleMessage(model, GlobalMessages.CONF_MESSAGES_HOLDER,
						"account.confirmation.forgotten.password.link.sent", new Object[]
						{ forgottenPassExpValue });
				model.addAttribute(new ForgottenPwdForm());
			}
			catch (final UnknownIdentifierException unknownIdentifierException)
			{
				GlobalMessages.addSingleMessage(model, GlobalMessages.CONF_MESSAGES_HOLDER,
						"account.confirmation.forgotten.password.link.sent", new Object[]
						{ forgottenPassExpValue });
				model.addAttribute(new ForgottenPwdForm());
				LOG.warn("Email: " + forgottenPwdForm.getEmail() + " does not exist in the database.");
			}
			return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPage;
		}
	}

	@RequestMapping(value = "/change", method = RequestMethod.GET)
	public String getChangePassword(@RequestParam(required = false) final String token, final Model model)
			throws CMSItemNotFoundException
	{
		if (StringUtils.isBlank(token))
		{
			return REDIRECT_HOME;
		}
		final UpdatePwdForm form = new UpdatePwdForm();
		form.setToken(token);
		model.addAttribute(form);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("updatePwd.title"));
		return ControllerConstants.Views.Pages.Password.PasswordResetChangePage;
	}

	@RequestMapping(value = "/change", method = RequestMethod.POST)
	public String changePassword(@Valid final UpdatePwdForm form, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			prepareErrorMessage(model, UPDATE_PWD_CMS_PAGE);
			return ControllerConstants.Views.Pages.Password.PasswordResetChangePage;
		}
		if (!StringUtils.isBlank(form.getToken()))
		{
			try
			{
				getCustomerFacade().updatePassword(form.getToken(), form.getPwd());
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
						"account.confirmation.password.updated");
			}
			catch (final TokenInvalidatedException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("update passwoed failed due to, " + e.getMessage(), e);
				}
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "updatePwd.token.invalidated");
			}
			catch (final RuntimeException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("update passwoed failed due to, " + e.getMessage(), e);
				}
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "updatePwd.token.invalid");
			}
		}
		return REDIRECT_LOGIN;
	}

	/**
	 * Prepares the view to display an error message
	 * 
	 * @param model
	 * @param page
	 * @throws CMSItemNotFoundException
	 */
	protected void prepareErrorMessage(final Model model, final String page) throws CMSItemNotFoundException
	{
		GlobalMessages.addErrorMessage(model, "form.global.error");
		storeCmsPageInModel(model, getContentPageForLabelOrId(page));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(page));
	}
}
