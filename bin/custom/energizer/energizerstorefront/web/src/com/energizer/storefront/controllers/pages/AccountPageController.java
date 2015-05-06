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

import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.b2bacceleratorfacades.api.cart.CartFacade;
import de.hybris.platform.b2bacceleratorfacades.order.B2BOrderFacade;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderApprovalData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderHistoryEntryData;
import de.hybris.platform.b2bacceleratorfacades.order.data.ScheduledCartData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercefacades.user.data.TitleData;
import de.hybris.platform.commercefacades.user.exceptions.PasswordMismatchException;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.workflow.enums.WorkflowActionType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade;
import com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade;
import com.energizer.facades.order.impl.DefaultEnergizerB2BOrderHistoryFacade;
import com.energizer.facades.quickorder.EnergizerQuickOrderFacade;
import com.energizer.quickorder.QuickOrderData;
import com.energizer.services.product.EnergizerProductService;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.energizer.storefront.breadcrumb.Breadcrumb;
import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.AddressForm;
import com.energizer.storefront.forms.OrderApprovalDecisionForm;
import com.energizer.storefront.forms.QuoteOrderForm;
import com.energizer.storefront.forms.ReorderForm;
import com.energizer.storefront.forms.UpdateEmailForm;
import com.energizer.storefront.forms.UpdatePasswordForm;
import com.energizer.storefront.forms.UpdateProfileForm;
import com.energizer.storefront.util.XSSFilterUtil;
import com.energizer.storefront.variants.VariantSortStrategy;


/**
 * Controller for home page.
 */
@Controller
@Scope("tenant")
@RequestMapping("/my-account")
public class AccountPageController extends AbstractSearchPageController
{
	// Internal Redirects
	private static final String REDIRECT_MY_ACCOUNT = REDIRECT_PREFIX + "/my-account";
	private static final String REDIRECT_TO_ADDRESS_BOOK_PAGE = REDIRECT_PREFIX + "/my-account/address-book";
	private static final String REDIRECT_TO_PAYMENT_INFO_PAGE = REDIRECT_PREFIX + "/my-account/payment-details";
	private static final String REDIRECT_TO_PROFILE_PAGE = REDIRECT_PREFIX + "/my-account/profile";
	private static final String REDIRECT_TO_MYREPLENISHMENTS_PAGE = REDIRECT_PREFIX + "/my-account/my-replenishment";
	private static final String REDIRECT_TO_MYREPLENISHMENTS_DETAIL_PAGE = REDIRECT_PREFIX + "/my-account/my-replenishment/%s/";
	private static final String REDIRECT_TO_QUOTES_DETAILS = REDIRECT_PREFIX + "/my-account/my-quote/%s";
	private static final String REDIRECT_MY_ACCOUNT_QUICK_ORDER = REDIRECT_PREFIX + "/my-account/quickorder";
	private static final String REDIRECT_QUICK_ORDER_TO_CART = REDIRECT_PREFIX + "/cart";
	/**
	 * We use this suffix pattern because of an issue with Spring 3.1 where a Uri value is incorrectly extracted if it
	 * contains on or more '.' characters. Please see https://jira.springsource.org/browse/SPR-6164 for a discussion on
	 * the issue and future resolution.
	 */
	private static final String ORDER_CODE_PATH_VARIABLE_PATTERN = "{orderCode:.*}";
	private static final String ADDRESS_CODE_PATH_VARIABLE_PATTERN = "{addressCode:.*}";
	private static final String JOB_CODE_PATH_VARIABLE_PATTERN = "{jobCode:.*}";
	private static final String WORKFLOW_ACTION_CODE_PATH_VARIABLE_PATTERN = "{workflowActionCode:.*}";

	// CMS Pages
	private static final String ACCOUNT_CMS_PAGE = "account";
	private static final String PROFILE_CMS_PAGE = "profile";
	private static final String ADDRESS_BOOK_CMS_PAGE = "address-book";
	private static final String ADD_EDIT_ADDRESS_CMS_PAGE = "add-edit-address";
	private static final String PAYMENT_DETAILS_CMS_PAGE = "payment-details";
	private static final String ORDER_HISTORY_CMS_PAGE = "orders";
	private static final String ORDER_DETAIL_CMS_PAGE = "order";
	private static final String MY_QUOTES_CMS_PAGE = "my-quotes";
	private static final String MY_REPLENISHMENT_ORDERS_CMS_PAGE = "my-replenishment-orders";
	private static final String ORDER_APPROVAL_DASHBOARD_CMS_PAGE = "order-approval-dashboard";

	private static final String QUICK_ORDER_PAGE = "quickorderpage";
	private static final String QUICK_ORDER_PAGE_URL = "/quickorder";
	private static final String QUICK_ORDER_ADD_ITEM_PAGE_URL = "/quickorder/addItem";
	private static final String QUICK_ORDER_REMOVE_ITEM_PAGE_URL = "/quickorder/removeItem";
	private static final String QUICK_ORDER_RESET_PAGE_URL = "/quickorder/resetQuickOrder";
	private static final String QUICK_ORDER_ADD_TO_CART_PAGE_URL = "/quickorder/addToCartAndContinue";
	private static final String QUICK_ORDER_AJAX_UPDATE_URL = "/quickorder/qtyAjaxUpdate";

	private static final String BUSINESS_RULE_ERRORS = "businessRuleError";

	private static final String EXCEL_UPLOAD_PAGE = "excelupload";


	private static final Logger LOG = Logger.getLogger(AccountPageController.class);

	@Resource(name = "b2bOrderFacade")
	private B2BOrderFacade orderFacade;

	@Resource(name = "checkoutFacade")
	private CheckoutFacade checkoutFacade;

	@Resource(name = "userFacade")
	protected UserFacade userFacade;

	@Resource(name = "b2bCustomerFacade")
	protected CustomerFacade customerFacade;

	@Resource(name = "accountBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder accountBreadcrumbBuilder;

	@Resource(name = "enumerationService")
	private EnumerationService enumerationService;

	@Resource(name = "variantSortStrategy")
	private VariantSortStrategy variantSortStrategy;
	//Customization of orders
	@Resource(name = "defaultEnergizerB2BOrderHistoryFacade")
	private DefaultEnergizerB2BOrderHistoryFacade orderHistoryFacade;
	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "b2bUnitService")
	private B2BUnitService b2bUnitService;

	@Resource(name = "quickOrderFacade")
	private EnergizerQuickOrderFacade quickOrderFacade;

	@Resource(name = "energizerCompanyB2BCommerceFacade")
	protected EnergizerCompanyB2BCommerceFacade energizerCompanyB2BCommerceFacade;

	@Resource(name = "energizerProductService")
	EnergizerProductService energizerProductService;

	@Resource(name = "orderEntryBusinessRulesService")
	EnergizerOrderEntryBusinessRuleValidationService orderEntryBusinessRulesService;

	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "energizerB2BCheckoutFlowFacade")
	private EnergizerB2BCheckoutFlowFacade energizerB2BCheckoutFlowFacade;

	@ModelAttribute("comments")
	public List<String> getApproverComments()
	{
		final List<String> approverComments = new ArrayList<String>();
		approverComments.add("Order placed successfully");
		approverComments.add("Order placed is not valid");
		approverComments.add("Invalid Order");
		approverComments.add("Out Of Stock");
		return approverComments;
	}



	@RequestMapping(method = RequestMethod.GET)
	@RequireHardLogIn
	public String account(final Model model) throws CMSItemNotFoundException
	{
		storeCmsPageInModel(model, getContentPageForLabelOrId(ACCOUNT_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ACCOUNT_CMS_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs(null));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountHomePage;
	}

	@RequestMapping(value = "/orders", method = RequestMethod.GET)
	@RequireHardLogIn
	public String orders(@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode, final Model model)
			throws CMSItemNotFoundException
	{ // Get all order statuses except for the Pending Quote status final List<OrderStatus>

		List<OrderStatus> validStates = enumerationService.getEnumerationValues(OrderStatus._TYPECODE);
		validStates = enumerationService.getEnumerationValues(OrderStatus._TYPECODE);
		validStates.remove(OrderStatus.PENDING_QUOTE);
		validStates.remove(OrderStatus.APPROVED_QUOTE);
		validStates.remove(OrderStatus.REJECTED_QUOTE);
		// Handle paged search results 
		final PageableData pageableData = createPageableData(page, 5, sortCode, showMode);
		final OrderStatus[] orderStatuses = validStates.toArray(new OrderStatus[validStates.size()]);
		final EnergizerB2BUnitModel energizerB2BUnitModel = energizerCompanyB2BCommerceFacade
				.getEnergizerB2BUnitModelForLoggedInUser();

		final SearchPageData<OrderHistoryData> searchPageData = orderHistoryFacade.getOrdersForB2BUnit(energizerB2BUnitModel,
				pageableData);
		populateModel(model, searchPageData, showMode);

		storeCmsPageInModel(model, getContentPageForLabelOrId(ORDER_HISTORY_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ORDER_HISTORY_CMS_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.orderHistory"));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountOrderHistoryPage;
	}

	@RequestMapping(value = "/order/" + ORDER_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String order(@PathVariable("orderCode") final String orderCode, final Model model) throws CMSItemNotFoundException
	{
		try
		{
			final OrderData orderDetails = orderFacade.getOrderDetailsForCode(orderCode);

			model.addAttribute("orderData", orderDetails);
			model.addAttribute(new ReorderForm());

			final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
			breadcrumbs.add(new Breadcrumb("/my-account/orders", getMessageSource().getMessage("text.account.orderHistory", null,
					getI18nService().getCurrentLocale()), null));
			breadcrumbs.add(new Breadcrumb("#", getMessageSource().getMessage("text.account.order.orderBreadcrumb", new Object[]
			{ orderDetails.getCode() }, "Order {0}", getI18nService().getCurrentLocale()), null));
			model.addAttribute("breadcrumbs", breadcrumbs);

		}
		catch (final UnknownIdentifierException e)
		{
			LOG.warn("Attempted to load a order that does not exist or is not visible", e);
			return REDIRECT_MY_ACCOUNT;
		}
		storeCmsPageInModel(model, getContentPageForLabelOrId(ORDER_DETAIL_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ORDER_DETAIL_CMS_PAGE));
		return ControllerConstants.Views.Pages.Account.AccountOrderPage;
	}

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	@RequireHardLogIn
	public String profile(final Model model) throws CMSItemNotFoundException
	{
		final List<TitleData> titles = userFacade.getTitles();

		final CustomerData customerData = customerFacade.getCurrentCustomer();
		//		customerData.setContactNumber(energizerCompanyB2BCommerceFacade.getContactNumber(customerData.getUid(), customerData));
		if (customerData.getTitleCode() != null)
		{
			model.addAttribute("title", CollectionUtils.find(titles, new Predicate()
			{
				@Override
				public boolean evaluate(final Object object)
				{
					if (object instanceof TitleData)
					{
						return customerData.getTitleCode().equals(((TitleData) object).getCode());
					}
					return false;
				}
			}));
		}

		model.addAttribute("customerData", customerData);

		storeCmsPageInModel(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.profile"));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountProfilePage;
	}

	@RequestMapping(value = "/update-email", method = RequestMethod.GET)
	@RequireHardLogIn
	public String editEmail(final Model model) throws CMSItemNotFoundException
	{
		final CustomerData customerData = customerFacade.getCurrentCustomer();
		final UpdateEmailForm updateEmailForm = new UpdateEmailForm();

		updateEmailForm.setEmail(customerData.getDisplayUid());

		model.addAttribute("updateEmailForm", updateEmailForm);

		storeCmsPageInModel(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.profile"));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountProfileEmailEditPage;
	}

	@RequestMapping(value = "/update-email", method = RequestMethod.POST)
	@RequireHardLogIn
	public String updateEmail(@Valid final UpdateEmailForm updateEmailForm, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{
		String returnAction = REDIRECT_TO_PROFILE_PAGE;

		if (!updateEmailForm.getEmail().equals(updateEmailForm.getChkEmail()))
		{
			bindingResult.rejectValue("chkEmail", "validation.checkEmail.equals", new Object[] {}, "validation.checkEmail.equals");
		}

		if (bindingResult.hasErrors())
		{
			returnAction = errorUpdatingEmail(model);
		}
		else
		{
			try
			{
				customerFacade.changeUid(updateEmailForm.getEmail(), updateEmailForm.getPassword());
				GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.CONF_MESSAGES_HOLDER,
						"text.account.profile.confirmationUpdated");

				// Replace the spring security authentication with the new UID
				final String newUid = customerFacade.getCurrentCustomer().getUid().toLowerCase();
				final Authentication oldAuthentication = SecurityContextHolder.getContext().getAuthentication();
				final UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(newUid, null,
						oldAuthentication.getAuthorities());
				newAuthentication.setDetails(oldAuthentication.getDetails());
				SecurityContextHolder.getContext().setAuthentication(newAuthentication);
			}
			catch (final DuplicateUidException e)
			{
				bindingResult.rejectValue("email", "profile.email.unique");
				returnAction = errorUpdatingEmail(model);
			}
			catch (final PasswordMismatchException passwordMismatchException)
			{
				bindingResult.rejectValue("email", "profile.currentPassword.invalid");
				returnAction = errorUpdatingEmail(model);
			}
		}

		return returnAction;
	}

	protected String errorUpdatingEmail(final Model model) throws CMSItemNotFoundException
	{
		final String returnAction;
		GlobalMessages.addErrorMessage(model, "form.global.error");
		storeCmsPageInModel(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.profile"));
		returnAction = ControllerConstants.Views.Pages.Account.AccountProfileEmailEditPage;
		return returnAction;
	}

	@RequestMapping(value = "/update-profile", method = RequestMethod.GET)
	@RequireHardLogIn
	public String editProfile(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute("titleData", userFacade.getTitles());

		final CustomerData customerData = customerFacade.getCurrentCustomer();
		final UpdateProfileForm updateProfileForm = new UpdateProfileForm();
		//		customerData.setContactNumber(energizerCompanyB2BCommerceFacade.getContactNumber(customerData.getUid(), customerData));
		updateProfileForm.setTitleCode(customerData.getTitleCode());
		updateProfileForm.setFirstName(customerData.getFirstName());
		updateProfileForm.setLastName(customerData.getLastName());
		updateProfileForm.setContactNumber(customerData.getContactNumber());
		model.addAttribute("updateProfileForm", updateProfileForm);

		storeCmsPageInModel(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));

		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.profile"));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountProfileEditPage;
	}

	@RequestMapping(value = "/update-profile", method = RequestMethod.POST)
	@RequireHardLogIn
	public String updateProfile(@Valid final UpdateProfileForm updateProfileForm, final BindingResult bindingResult,
			final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{
		String returnAction = ControllerConstants.Views.Pages.Account.AccountProfileEditPage;
		final CustomerData currentCustomerData = customerFacade.getCurrentCustomer();
		final CustomerData customerData = new CustomerData();
		customerData.setTitleCode(updateProfileForm.getTitleCode());
		customerData.setFirstName(updateProfileForm.getFirstName());
		customerData.setLastName(updateProfileForm.getLastName());
		customerData.setUid(currentCustomerData.getUid());
		customerData.setDisplayUid(currentCustomerData.getDisplayUid());
		customerData.setContactNumber(updateProfileForm.getContactNumber());
		model.addAttribute("titleData", userFacade.getTitles());

		if (bindingResult.hasErrors())
		{
			model.addAttribute("titleData", userFacade.getTitles());
			GlobalMessages.addErrorMessage(model, "form.global.error");
		}
		else
		{
			try
			{
				//				customerFacade.updateProfile(customerData);
				energizerCompanyB2BCommerceFacade.updateProfile(customerData);
				GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.CONF_MESSAGES_HOLDER,
						"text.account.profile.confirmationUpdated");
				returnAction = REDIRECT_TO_PROFILE_PAGE;
			}
			catch (final DuplicateUidException e)
			{
				bindingResult.rejectValue("email", "registration.error.account.exists.title");
				GlobalMessages.addErrorMessage(model, "form.global.error");
			}
		}

		storeCmsPageInModel(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.profile"));
		return returnAction;
	}

	@RequestMapping(value = "/update-password", method = RequestMethod.GET)
	@RequireHardLogIn
	public String updatePassword(final Model model) throws CMSItemNotFoundException
	{
		final UpdatePasswordForm updatePasswordForm = new UpdatePasswordForm();

		model.addAttribute("updatePasswordForm", updatePasswordForm);

		storeCmsPageInModel(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));

		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.profile.updatePasswordForm"));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountChangePasswordPage;
	}

	@RequestMapping(value = "/update-password", method = RequestMethod.POST)
	@RequireHardLogIn
	public String updatePassword(@Valid final UpdatePasswordForm updatePasswordForm, final BindingResult bindingResult,
			final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{

		if (!bindingResult.hasErrors())
		{
			if (updatePasswordForm.getNewPassword().equals(updatePasswordForm.getCheckNewPassword()))
			{
				try
				{
					customerFacade.changePassword(updatePasswordForm.getCurrentPassword(), updatePasswordForm.getNewPassword());
				}
				catch (final PasswordMismatchException localException)
				{
					bindingResult.rejectValue("currentPassword", "profile.currentPassword.invalid", new Object[] {},
							"profile.currentPassword.invalid");
				}

			}
			else
			{
				bindingResult.rejectValue("checkNewPassword", "validation.checkPwd.equals", new Object[] {},
						"validation.checkPwd.equals");
			}
		}

		if (bindingResult.hasErrors())
		{
			GlobalMessages.addErrorMessage(model, "form.global.error");
			storeCmsPageInModel(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PROFILE_CMS_PAGE));

			model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.profile.updatePasswordForm"));
			return ControllerConstants.Views.Pages.Account.AccountChangePasswordPage;
		}
		else
		{
			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.CONF_MESSAGES_HOLDER,
					"text.account.confirmation.password.updated");
			return REDIRECT_TO_PROFILE_PAGE;
		}
	}

	@RequestMapping(value = "/address-book", method = RequestMethod.GET)
	@RequireHardLogIn
	public String getAddressBook(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute("addressData", userFacade.getAddressBook());

		storeCmsPageInModel(model, getContentPageForLabelOrId(ADDRESS_BOOK_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADDRESS_BOOK_CMS_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.addressBook"));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountAddressBookPage;
	}

	@RequestMapping(value = "/add-address", method = RequestMethod.GET)
	@RequireHardLogIn
	public String addAddress(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute("countryData", checkoutFacade.getDeliveryCountries());
		model.addAttribute("titleData", userFacade.getTitles());
		model.addAttribute("addressForm", new AddressForm());
		model.addAttribute("addressBookEmpty", Boolean.valueOf(userFacade.isAddressBookEmpty()));

		storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));

		final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
		breadcrumbs.add(new Breadcrumb("/my-account/address-book", getMessageSource().getMessage("text.account.addressBook", null,
				getI18nService().getCurrentLocale()), null));
		breadcrumbs.add(new Breadcrumb("#", getMessageSource().getMessage("text.account.addressBook.addEditAddress", null,
				getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountEditAddressPage;
	}

	@RequestMapping(value = "/add-address", method = RequestMethod.POST)
	@RequireHardLogIn
	public String addAddress(@Valid final AddressForm addressForm, final BindingResult bindingResult, final Model model,
			final HttpServletRequest request, final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			GlobalMessages.addErrorMessage(model, "form.global.error");
			storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
			model.addAttribute("countryData", checkoutFacade.getDeliveryCountries());
			model.addAttribute("titleData", userFacade.getTitles());
			return ControllerConstants.Views.Pages.Account.AccountEditAddressPage;
		}

		final AddressData newAddress = new AddressData();
		newAddress.setTitleCode(addressForm.getTitleCode());
		newAddress.setFirstName(addressForm.getFirstName());
		newAddress.setLastName(addressForm.getLastName());
		newAddress.setLine1(addressForm.getLine1());
		newAddress.setLine2(addressForm.getLine2());
		newAddress.setTown(addressForm.getTownCity());
		newAddress.setPostalCode(addressForm.getPostcode());
		newAddress.setBillingAddress(false);
		newAddress.setShippingAddress(true);
		newAddress.setVisibleInAddressBook(true);

		final CountryData countryData = new CountryData();

		countryData.setIsocode(addressForm.getCountryIso());
		newAddress.setCountry(countryData);

		if (userFacade.isAddressBookEmpty())
		{
			newAddress.setDefaultAddress(true);
		}
		else
		{
			newAddress.setDefaultAddress(addressForm.getDefaultAddress().booleanValue());
		}
		userFacade.addAddress(newAddress);
		GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER, "account.confirmation.address.added");

		return REDIRECT_TO_ADDRESS_BOOK_PAGE;
	}

	@RequestMapping(value = "/edit-address/" + ADDRESS_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String editAddress(@PathVariable("addressCode") final String addressCode, final Model model)
			throws CMSItemNotFoundException
	{
		final AddressForm addressForm = new AddressForm();
		model.addAttribute("countryData", checkoutFacade.getDeliveryCountries());
		model.addAttribute("titleData", userFacade.getTitles());
		model.addAttribute("addressForm", addressForm);
		model.addAttribute("addressBookEmpty", Boolean.valueOf(userFacade.isAddressBookEmpty()));

		for (final AddressData addressData : userFacade.getAddressBook())
		{
			if (addressData.getId() != null && addressData.getId().equals(addressCode))
			{
				model.addAttribute("addressData", addressData);
				addressForm.setAddressId(addressData.getId());
				addressForm.setTitleCode(addressData.getTitleCode());
				addressForm.setFirstName(addressData.getFirstName());
				addressForm.setLastName(addressData.getLastName());
				addressForm.setLine1(addressData.getLine1());
				addressForm.setLine2(addressData.getLine2());
				addressForm.setTownCity(addressData.getTown());
				addressForm.setPostcode(addressData.getPostalCode());
				addressForm.setCountryIso(addressData.getCountry().getIsocode());
				if (userFacade.getDefaultAddress() != null && userFacade.getDefaultAddress().getId() != null
						&& userFacade.getDefaultAddress().getId().equals(addressData.getId()))
				{
					addressForm.setDefaultAddress(Boolean.TRUE);
				}
				break;
			}
		}

		storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));

		final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
		breadcrumbs.add(new Breadcrumb("/my-account/address-book", getMessageSource().getMessage("text.account.addressBook", null,
				getI18nService().getCurrentLocale()), null));
		breadcrumbs.add(new Breadcrumb("#", getMessageSource().getMessage("text.account.addressBook.addEditAddress", null,
				getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountEditAddressPage;
	}

	@RequestMapping(value = "/edit-address/" + ADDRESS_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.POST)
	@RequireHardLogIn
	public String editAddress(@Valid final AddressForm addressForm, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		model.addAttribute("metaRobots", "no-index,no-follow");
		if (bindingResult.hasErrors())
		{
			GlobalMessages.addErrorMessage(model, "form.global.error");
			storeCmsPageInModel(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
			model.addAttribute("countryData", checkoutFacade.getDeliveryCountries());
			model.addAttribute("titleData", userFacade.getTitles());
			return ControllerConstants.Views.Pages.Account.AccountEditAddressPage;
		}

		final AddressData newAddress = new AddressData();
		newAddress.setId(addressForm.getAddressId());
		newAddress.setTitleCode(addressForm.getTitleCode());
		newAddress.setFirstName(addressForm.getFirstName());
		newAddress.setLastName(addressForm.getLastName());
		newAddress.setLine1(addressForm.getLine1());
		newAddress.setLine2(addressForm.getLine2());
		newAddress.setTown(addressForm.getTownCity());
		newAddress.setPostalCode(addressForm.getPostcode());
		newAddress.setBillingAddress(false);
		newAddress.setShippingAddress(true);
		newAddress.setVisibleInAddressBook(true);

		final CountryData countryData = new CountryData();

		countryData.setIsocode(addressForm.getCountryIso());
		newAddress.setCountry(countryData);

		if (Boolean.TRUE.equals(addressForm.getDefaultAddress()) || userFacade.getAddressBook().size() <= 1)
		{
			newAddress.setDefaultAddress(true);
		}
		else
		{
			newAddress.setDefaultAddress(addressForm.getDefaultAddress() != null && addressForm.getDefaultAddress().booleanValue());
		}
		userFacade.editAddress(newAddress);

		GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
				"text.account.addressBook.confirmationUpdated");
		return REDIRECT_TO_ADDRESS_BOOK_PAGE;
	}

	@RequestMapping(value = "/remove-address/" + ADDRESS_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String removeAddress(@PathVariable("addressCode") final String addressCode, final RedirectAttributes redirectModel)
	{
		final AddressData addressData = new AddressData();
		addressData.setId(addressCode);
		userFacade.removeAddress(addressData);
		GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER, "account.confirmation.address.removed");
		return REDIRECT_TO_ADDRESS_BOOK_PAGE;
	}

	@RequestMapping(value = "/set-default-address/" + ADDRESS_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String setDefaultAddress(@PathVariable("addressCode") final String addressCode, final RedirectAttributes redirectModel)
	{
		final AddressData addressData = new AddressData();
		addressData.setDefaultAddress(true);
		addressData.setVisibleInAddressBook(true);
		addressData.setId(addressCode);
		userFacade.setDefaultAddress(addressData);
		GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
				"account.confirmation.default.address.changed");
		return REDIRECT_TO_ADDRESS_BOOK_PAGE;
	}

	@RequestMapping(value = "/payment-details", method = RequestMethod.GET)
	@RequireHardLogIn
	public String paymentDetails(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute("customerData", customerFacade.getCurrentCustomer());
		model.addAttribute("paymentInfoData", userFacade.getCCPaymentInfos(true));
		storeCmsPageInModel(model, getContentPageForLabelOrId(PAYMENT_DETAILS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ADD_EDIT_ADDRESS_CMS_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.paymentDetails"));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountPaymentInfoPage;
	}

	@RequestMapping(value = "/set-default-payment-details", method = RequestMethod.POST)
	@RequireHardLogIn
	public String setDefaultPaymentDetails(@RequestParam final String paymentInfoId)
	{
		CCPaymentInfoData paymentInfoData = null;
		if (StringUtils.isNotBlank(paymentInfoId))
		{
			paymentInfoData = userFacade.getCCPaymentInfoForCode(paymentInfoId);
		}
		userFacade.setDefaultPaymentInfo(paymentInfoData);
		return REDIRECT_TO_PAYMENT_INFO_PAGE;
	}

	@RequestMapping(value = "/remove-payment-method", method = RequestMethod.POST)
	@RequireHardLogIn
	public String removePaymentMethod(final Model model, @RequestParam(value = "paymentInfoId") final String paymentMethodId,
			final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{
		userFacade.unlinkCCPaymentInfo(paymentMethodId);
		GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.CONF_MESSAGES_HOLDER,
				"text.account.profile.paymentCart.removed");
		return REDIRECT_TO_PAYMENT_INFO_PAGE;
	}

	@RequestMapping(value = "/orderApprovalDetails/" + WORKFLOW_ACTION_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String orderApprovalDetails(@PathVariable("workflowActionCode") final String workflowActionCode, final Model model)
			throws CMSItemNotFoundException
	{
		try
		{
			final B2BOrderApprovalData orderApprovalDetails = orderFacade.getOrderApprovalDetailsForCode(workflowActionCode);
			model.addAttribute("orderApprovalData", orderApprovalDetails);
			if (!model.containsAttribute("orderApprovalDecisionForm"))
			{
				model.addAttribute("orderApprovalDecisionForm", new OrderApprovalDecisionForm());
			}

			final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
			breadcrumbs.add(new Breadcrumb("/my-account/approval-dashboard", getMessageSource().getMessage(
					"text.account.orderApprovalDashboard", null, getI18nService().getCurrentLocale()), null));
			breadcrumbs.add(new Breadcrumb("#", getMessageSource().getMessage("text.account.order.orderBreadcrumb", new Object[]
			{ orderApprovalDetails.getB2bOrderData().getCode() }, "Order {0}", getI18nService().getCurrentLocale()), null));

			model.addAttribute("breadcrumbs", breadcrumbs);

		}
		catch (final UnknownIdentifierException e)
		{
			LOG.warn("Attempted to load a order that does not exist or is not visible", e);
			return REDIRECT_MY_ACCOUNT;
		}
		storeCmsPageInModel(model, getContentPageForLabelOrId(ORDER_DETAIL_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ORDER_DETAIL_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountOrderApprovalDetailsPage;
	}

	@RequestMapping(value = "/order/approvalDecision", method = RequestMethod.POST)
	@RequireHardLogIn
	public String orderApprovalDecision(
			@ModelAttribute("orderApprovalDecisionForm") final OrderApprovalDecisionForm orderApprovalDecisionForm,
			final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{
		try
		{
			if ("REJECT".contains(orderApprovalDecisionForm.getApproverSelectedDecision())
					&& StringUtils.isEmpty(orderApprovalDecisionForm.getComments()))
			{
				GlobalMessages.addErrorMessage(model, "text.account.orderApproval.addApproverComments");
				model.addAttribute("orderApprovalDecisionForm", orderApprovalDecisionForm);
				return orderApprovalDetails(orderApprovalDecisionForm.getWorkFlowActionCode(), model);
			}

			B2BOrderApprovalData b2bOrderApprovalData = new B2BOrderApprovalData();
			b2bOrderApprovalData.setSelectedDecision(orderApprovalDecisionForm.getApproverSelectedDecision());
			b2bOrderApprovalData.setApprovalComments(orderApprovalDecisionForm.getComments());
			b2bOrderApprovalData.setWorkflowActionModelCode(orderApprovalDecisionForm.getWorkFlowActionCode());

			b2bOrderApprovalData = orderFacade.setOrderApprovalDecision(b2bOrderApprovalData);
			energizerB2BCheckoutFlowFacade.setOrderApprover((EnergizerB2BCustomerModel) userService.getCurrentUser(),
					b2bOrderApprovalData.getB2bOrderData().getCode());

		}
		catch (final Exception e)
		{
			LOG.warn("Attempted to load a order that does not exist or is not visible", e);
			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.CONF_MESSAGES_HOLDER,
					"text.account.profile.paymentCart.removed");

			return REDIRECT_MY_ACCOUNT;
		}

		return REDIRECT_MY_ACCOUNT + "/orderApprovalDetails/" + orderApprovalDecisionForm.getWorkFlowActionCode();
	}

	@RequestMapping(value = "/my-quotes", method = RequestMethod.GET)
	@RequireHardLogIn
	public String myQuotes(@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode, final Model model)
			throws CMSItemNotFoundException
	{
		// Handle paged search results
		final PageableData pageableData = createPageableData(page, 5, sortCode, showMode);
		final SearchPageData<OrderHistoryData> searchPageData = orderFacade.getPagedOrderHistoryForStatuses(pageableData,
				OrderStatus.PENDING_QUOTE, OrderStatus.APPROVED_QUOTE, OrderStatus.REJECTED_QUOTE);
		populateModel(model, searchPageData, showMode);
		model.addAttribute(new ReorderForm());

		final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
		breadcrumbs.add(new Breadcrumb("/my-account/my-quotes", getMessageSource().getMessage(
				"text.account.manageQuotes.breadcrumb", null, getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		storeCmsPageInModel(model, getContentPageForLabelOrId(MY_QUOTES_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MY_QUOTES_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountMyQuotesPage;
	}

	@RequestMapping(value = "/my-quote/" + ORDER_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String quotesDetails(@PathVariable("orderCode") final String orderCode, final Model model)
			throws CMSItemNotFoundException
	{
		try
		{
			final OrderData orderDetails = orderFacade.getOrderDetailsForCode(orderCode);

			model.addAttribute("orderData", orderDetails);

			final List<B2BOrderHistoryEntryData> orderHistoryEntries = orderFacade.getOrderHistoryEntryData(orderCode);
			model.addAttribute("orderHistoryEntryData", orderHistoryEntries);

			model.addAttribute(new ReorderForm());

			if (!model.containsAttribute("quoteOrderDecisionForm"))
			{
				model.addAttribute("quoteOrderDecisionForm", new QuoteOrderForm());
			}

			final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
			breadcrumbs.add(new Breadcrumb("/my-account/my-quotes", getMessageSource().getMessage(
					"text.account.manageQuotes.breadcrumb", null, getI18nService().getCurrentLocale()), null));
			breadcrumbs.add(new Breadcrumb("/my-account/my-quotes/" + orderDetails.getCode(), getMessageSource().getMessage(
					"text.account.manageQuotes.details.breadcrumb", new Object[]
					{ orderDetails.getCode() }, "Quote Details {0}", getI18nService().getCurrentLocale()), null));
			model.addAttribute("breadcrumbs", breadcrumbs);

		}
		catch (final UnknownIdentifierException e)
		{
			LOG.warn("Attempted to load a order that does not exist or is not visible", e);
			return REDIRECT_MY_ACCOUNT;
		}
		storeCmsPageInModel(model, getContentPageForLabelOrId(MY_QUOTES_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MY_QUOTES_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountQuoteDetailPage;
	}

	@RequestMapping(value = "/quote/quoteOrderDecision")
	@RequireHardLogIn
	public String quoteOrderDecision(@ModelAttribute("quoteOrderDecisionForm") final QuoteOrderForm quoteOrderForm,
			final Model model, final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		storeCmsPageInModel(model, getContentPageForLabelOrId(MY_QUOTES_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MY_QUOTES_CMS_PAGE));
		String orderCode = null;
		try
		{
			orderCode = quoteOrderForm.getOrderCode();

			final String comment = XSSFilterUtil.filter(quoteOrderForm.getComments());

			if ("NEGOTIATEQUOTE".equals(quoteOrderForm.getSelectedQuoteDecision()))
			{
				if (StringUtils.isBlank(comment))
				{
					setUpCommentIsEmptyError(quoteOrderForm, model);
					return quotesDetails(orderCode, model);
				}
				orderFacade.createAndSetNewOrderFromNegotiateQuote(orderCode, comment);
			}

			if ("ACCEPTQUOTE".equals(quoteOrderForm.getSelectedQuoteDecision()))
			{
				final OrderData orderDetails = orderFacade.getOrderDetailsForCode(orderCode);
				final Date quoteExpirationDate = orderDetails.getQuoteExpirationDate();
				if (quoteExpirationDate != null && quoteExpirationDate.before(new Date()))
				{
					GlobalMessages.addErrorMessage(model, "text.quote.expired");
					return quotesDetails(orderCode, model);
				}
				orderFacade.createAndSetNewOrderFromApprovedQuote(orderCode, comment);
				return REDIRECT_PREFIX + "/checkout/orderConfirmation/" + orderCode;
			}

			if ("CANCELQUOTE".equals(quoteOrderForm.getSelectedQuoteDecision()))
			{
				orderFacade.cancelOrder(orderCode, comment);
			}

			if ("ADDADDITIONALCOMMENT".equals(quoteOrderForm.getSelectedQuoteDecision()))
			{
				if (StringUtils.isBlank(comment))
				{
					setUpCommentIsEmptyError(quoteOrderForm, model);
					return quotesDetails(orderCode, model);
				}
				orderFacade.addAdditionalComment(orderCode, comment);
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
						"text.confirmation.quote.comment.added");
				return String.format(REDIRECT_TO_QUOTES_DETAILS, orderCode);
			}
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.warn("Attempted to load a order that does not exist or is not visible", e);
			return REDIRECT_MY_ACCOUNT;
		}

		return REDIRECT_PREFIX + "/checkout/quoteOrderConfirmation/" + orderCode;
	}

	protected void setUpCommentIsEmptyError(final QuoteOrderForm quoteOrderForm, final Model model)
			throws CMSItemNotFoundException
	{
		quoteOrderForm.setNegotiateQuote(true);
		model.addAttribute("quoteOrderDecisionForm", quoteOrderForm);
		GlobalMessages.addErrorMessage(model, "text.quote.empty");
	}

	@RequestMapping(value = "/my-replenishment", method = RequestMethod.GET)
	@RequireHardLogIn
	public String myReplenishment(@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode, final Model model)
			throws CMSItemNotFoundException
	{
		final PageableData pageableData = createPageableData(page, 5, sortCode, showMode);
		final SearchPageData<? extends ScheduledCartData> searchPageData = orderFacade.getPagedReplenishmentHistory(pageableData);
		populateModel(model, searchPageData, showMode);
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.manageReplenishment"));
		storeCmsPageInModel(model, getContentPageForLabelOrId(MY_REPLENISHMENT_ORDERS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MY_REPLENISHMENT_ORDERS_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountReplenishmentSchedule;
	}

	@RequestMapping(value = "/my-replenishment/" + JOB_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String replenishmentDetails(@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode, @PathVariable("jobCode") final String jobCode,
			final Model model) throws CMSItemNotFoundException
	{
		final ScheduledCartData scheduledCartData = orderFacade.getReplenishmentOrderDetailsForCode(jobCode, customerFacade
				.getCurrentCustomer().getUid());
		model.addAttribute("scheduleData", scheduledCartData);

		final PageableData pageableData = createPageableData(page, 5, sortCode, showMode);
		final SearchPageData<? extends OrderHistoryData> searchPageData = orderFacade.getPagedReplenishmentOrderHistory(jobCode,
				pageableData);
		populateModel(model, searchPageData, showMode);
		final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
		breadcrumbs.add(new Breadcrumb("/my-account/my-replenishment", getMessageSource().getMessage(
				"text.account.manageReplenishment", null, getI18nService().getCurrentLocale()), null));
		breadcrumbs.add(new Breadcrumb(String.format("/my-account/my-replenishment/%s/", jobCode), getMessageSource().getMessage(
				"text.account.replenishment.replenishmentBreadcrumb", new Object[]
				{ jobCode }, "Replenishment Orders {0}", getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		storeCmsPageInModel(model, getContentPageForLabelOrId(MY_REPLENISHMENT_ORDERS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MY_REPLENISHMENT_ORDERS_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountReplenishmentScheduleDetails;
	}

	@RequestMapping(value = "/my-replenishment/cancel/" + JOB_CODE_PATH_VARIABLE_PATTERN, method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public String cancelReplenishment(@PathVariable("jobCode") final String jobCode, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		this.orderFacade.cancelReplenishment(jobCode, customerFacade.getCurrentCustomer().getUid());
		GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
				"text.account.replenishment.confirmation.canceled");
		return REDIRECT_TO_MYREPLENISHMENTS_PAGE;
	}

	@RequestMapping(value = "/my-replenishment/detail/cancel/" + JOB_CODE_PATH_VARIABLE_PATTERN, method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public String cancelReplenishmentFromDetailPage(@PathVariable("jobCode") final String jobCode, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		this.orderFacade.cancelReplenishment(jobCode, customerFacade.getCurrentCustomer().getUid());
		GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
				"text.account.replenishment.confirmation.canceled");
		return String.format(REDIRECT_TO_MYREPLENISHMENTS_DETAIL_PAGE, jobCode);
	}

	@RequestMapping(value = "/my-replenishment/detail/confirmation/cancel/" + JOB_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String confirmCancelReplenishmentFromDetailsPage(@PathVariable("jobCode") final String jobCode, final Model model,
			final HttpServletRequest request) throws CMSItemNotFoundException
	{
		final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
		breadcrumbs.add(new Breadcrumb("/my-account/my-replenishment", getMessageSource().getMessage(
				"text.account.manageReplenishment", null, getI18nService().getCurrentLocale()), null));
		breadcrumbs.add(new Breadcrumb(String.format("/my-account/my-replenishment/%s/", jobCode), getMessageSource().getMessage(
				"text.account.replenishment.replenishmentBreadcrumb", new Object[]
				{ jobCode }, "Replenishment Orders {0}", getI18nService().getCurrentLocale()), null));
		breadcrumbs.add(new Breadcrumb("#", getMessageSource().getMessage(
				"text.account.manageReplenishment.confirm.cancel.breadcrumb", new Object[]
				{ jobCode }, "Remove Replenishment Schedule {0}", getI18nService().getCurrentLocale()), null));

		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute("arguments", String.format("%s", jobCode));
		model.addAttribute("page", "replenishment");
		model.addAttribute("disableUrl",
				String.format("%s/my-account/my-replenishment/detail/cancel/%s", request.getContextPath(), jobCode));
		model.addAttribute("cancelUrl", String.format("%s/my-account/my-replenishment/%s", request.getContextPath(), jobCode));
		storeCmsPageInModel(model, getContentPageForLabelOrId(MY_REPLENISHMENT_ORDERS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MY_REPLENISHMENT_ORDERS_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountCancelActionConfirmationPage;
	}

	@RequestMapping(value = "/my-replenishment/confirmation/cancel/" + JOB_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String confirmCancelReplenishment(@PathVariable("jobCode") final String jobCode, final Model model,
			final HttpServletRequest request) throws CMSItemNotFoundException
	{
		final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
		breadcrumbs.add(new Breadcrumb("/my-account/my-replenishment", getMessageSource().getMessage(
				"text.account.manageReplenishment", null, getI18nService().getCurrentLocale()), null));
		breadcrumbs.add(new Breadcrumb("#", getMessageSource().getMessage(
				"text.account.manageReplenishment.confirm.cancel.breadcrumb", new Object[]
				{ jobCode }, "Remove Replenishment Schedule {0}", getI18nService().getCurrentLocale()), null));

		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute("arguments", String.format("%s", jobCode));
		model.addAttribute("page", "replenishment");
		model.addAttribute("disableUrl",
				String.format("%s/my-account/my-replenishment/cancel/%s", request.getContextPath(), jobCode));
		model.addAttribute("cancelUrl", String.format("%s/my-account/my-replenishment/%s", request.getContextPath(), jobCode));
		storeCmsPageInModel(model, getContentPageForLabelOrId(MY_REPLENISHMENT_ORDERS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MY_REPLENISHMENT_ORDERS_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountCancelActionConfirmationPage;
	}

	@RequestMapping(value = "/approval-dashboard", method = RequestMethod.GET)
	@RequireHardLogIn
	public String orderApprovalDashboard(@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
			@RequestParam(value = "sort", required = false) final String sortCode, final Model model)
			throws CMSItemNotFoundException
	{
		final PageableData pageableData = createPageableData(page, 5, sortCode, showMode);
		final SearchPageData<? extends B2BOrderApprovalData> searchPageData = orderFacade.getPagedOrdersForApproval(
				new WorkflowActionType[]
				{ WorkflowActionType.START }, pageableData);
		populateModel(model, searchPageData, showMode);

		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.orderApprovalDashboard"));
		storeCmsPageInModel(model, getContentPageForLabelOrId(ORDER_APPROVAL_DASHBOARD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ORDER_APPROVAL_DASHBOARD_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountOrderApprovalDashboardPage;
	}

	@RequestMapping(value = "/my-replenishment/" + JOB_CODE_PATH_VARIABLE_PATTERN + "/" + ORDER_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String replenishmentOrderDetail(@PathVariable("jobCode") final String jobCode,
			@PathVariable("orderCode") final String orderCode, final Model model) throws CMSItemNotFoundException
	{
		try
		{
			model.addAttribute("orderData", orderFacade.getOrderDetailsForCode(orderCode));
			model.addAttribute("scheduleData",
					orderFacade.getReplenishmentOrderDetailsForCode(jobCode, customerFacade.getCurrentCustomer().getUid()));
			model.addAttribute(new ReorderForm());

			final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
			breadcrumbs.add(new Breadcrumb("/my-account/my-replenishment", getMessageSource().getMessage(
					"text.account.manageReplenishment", null, getI18nService().getCurrentLocale()), null));
			breadcrumbs.add(new Breadcrumb(String.format("/my-account/my-replenishment/%s/", jobCode), getMessageSource()
					.getMessage("text.account.replenishment.replenishmentBreadcrumb", new Object[]
					{ jobCode }, "Replenishment {0}", getI18nService().getCurrentLocale()), null));
			breadcrumbs.add(new Breadcrumb(String.format("/my-account/my-replenishment/%s/%s/", jobCode, orderCode),
					getMessageSource().getMessage("text.account.replenishment.replenishmentOrderDetailBreadcrumb", new Object[]
					{ orderCode }, "Order {0}", getI18nService().getCurrentLocale()), null));
			model.addAttribute("breadcrumbs", breadcrumbs);
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.warn("Attempted to load a order that does not exist or is not visible", e);
			return REDIRECT_MY_ACCOUNT;
		}
		storeCmsPageInModel(model, getContentPageForLabelOrId(MY_REPLENISHMENT_ORDERS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MY_REPLENISHMENT_ORDERS_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountOrderPage;
	}

	@RequestMapping(value = "/orderApproval/" + ORDER_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public String orderApproval(@PathVariable("orderCode") final String orderCode, final Model model)
			throws CMSItemNotFoundException
	{
		try
		{
			final OrderData orderDetails = orderFacade.getOrderDetailsForCode(orderCode);

			model.addAttribute("orderData", orderDetails);

			final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
			breadcrumbs.add(new Breadcrumb("/my-account/orders", getMessageSource().getMessage(
					"text.account.orderApprovalDashboard", null, getI18nService().getCurrentLocale()), null));
			breadcrumbs.add(new Breadcrumb("#", getMessageSource().getMessage("text.account.order.orderBreadcrumb", new Object[]
			{ orderDetails.getCode() }, "Order {0}", getI18nService().getCurrentLocale()), null));
			model.addAttribute("breadcrumbs", breadcrumbs);
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.warn("Attempted to load a order that does not exist or is not visible", e);
			return REDIRECT_MY_ACCOUNT;
		}
		storeCmsPageInModel(model, getContentPageForLabelOrId(ORDER_DETAIL_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ORDER_DETAIL_CMS_PAGE));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountOrderApprovalDetailsPage;
	}


	@RequestMapping(value = QUICK_ORDER_PAGE_URL, method = RequestMethod.GET)
	@RequireHardLogIn
	public String quickOrder(final Model model, final HttpSession session) throws CMSItemNotFoundException
	{
		LOG.info("Here in quick order page");
		final QuickOrderData quickOrder = quickOrderFacade.getQuickOrderFromSession((QuickOrderData) session
				.getAttribute(EnergizerQuickOrderFacade.QUICK_ORDER_SESSION_ATTRIBUTE));

		if (quickOrder != null && quickOrder.getLineItems().size() > 0)
		{
			model.addAttribute("orderform", quickOrder);
		}
		else
		{
			model.addAttribute("orderform", null);
		}
		model.addAttribute("cartData", quickOrderFacade.getCurrentSessionCart());
		storeCmsPageInModel(model, getContentPageForLabelOrId(QUICK_ORDER_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(QUICK_ORDER_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.quickorder.pageHeading"));
		model.addAttribute("metaRobots", "no-index,no-follow");



		return ControllerConstants.Views.Pages.Account.AccountQuickOrderPage;
	}

	@RequestMapping(value = QUICK_ORDER_ADD_ITEM_PAGE_URL, method = RequestMethod.POST)
	@RequireHardLogIn
	public String quickOrderAddItem(final Model model, @RequestParam("energizerMaterialID") final String energizerMaterialID,
			@RequestParam("distributorMaterialID") final String distributorMaterialID, final HttpSession session,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		final QuickOrderData quickOrder = quickOrderFacade.getQuickOrderFromSession((QuickOrderData) session
				.getAttribute(EnergizerQuickOrderFacade.QUICK_ORDER_SESSION_ATTRIBUTE));
		//quickOrderFacade.addItemToQuickOrder(quickOrder, energizerMaterialID, distributorMaterialID);
		//fetch and set the UOM and MOQ for the product for the customer
		final EnergizerCMIRModel cmir = quickOrderFacade.getCMIRForProductCodeOrCustomerMaterialID(energizerMaterialID,
				distributorMaterialID);
		if (orderEntryBusinessRulesService.getErrors() != null && !orderEntryBusinessRulesService.getErrors().isEmpty())
		{
			orderEntryBusinessRulesService.getErrors().clear();
		}
		if (cmir == null)
		{
			//GlobalMessages.addErrorMessage(model, "quickorder.addtocart.nocmir");
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
					Localization.getLocalizedString("quickorder.addtocart.nocmir"));
		}
		else
		{
			if (quickOrderFacade.productExistsInList(cmir.getErpMaterialId(), quickOrder))
			{
				quickOrderFacade.addItemToQuickOrder(quickOrder, cmir.getErpMaterialId(), cmir.getCustomerMaterialId());
			}
			else
			{
				final OrderEntryData orderEntry = quickOrderFacade.getProductData(energizerMaterialID, distributorMaterialID, cmir);
				if (orderEntry != null)
				{
					orderEntryBusinessRulesService.validateBusinessRules(orderEntry);
					//run the business rules on the product
					if (orderEntryBusinessRulesService.hasErrors())
					{
						//model.addAttribute(BUSINESS_RULE_ERRORS, orderEntryBusinessRulesService.getErrors());
						for (final BusinessRuleError error : orderEntryBusinessRulesService.getErrors())
						{
							LOG.info("The error message is " + error.getMessage());
							//GlobalMessages.addBusinessRuleMessage(model, error.getMessage());
							GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, error.getMessage());

						}
					}
					else
					{
						quickOrderFacade.addItemToQuickOrder(quickOrder, orderEntry.getProduct().getCode(), orderEntry.getProduct()
								.getCustomerMaterialId());
					}
				}
				else
				{
					//GlobalMessages.addErrorMessage(model, "quickorder.addtocart.cmir.badData");
					GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
							"quickorder.addtocart.cmir.badData");
				}
			}

		}

		//add it to the list if the product passes the business rules
		//or show messages on screen about the error as a result of a business rule validation failure
		session.setAttribute(EnergizerQuickOrderFacade.QUICK_ORDER_SESSION_ATTRIBUTE, quickOrder);
		model.addAttribute("orderform", quickOrder);

		storeCmsPageInModel(model, getContentPageForLabelOrId(QUICK_ORDER_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(QUICK_ORDER_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs(null));
		model.addAttribute("metaRobots", "no-index,no-follow");

		//return ControllerConstants.Views.Pages.Account.AccountQuickOrderPage;
		return REDIRECT_MY_ACCOUNT_QUICK_ORDER;
	}

	@RequestMapping(value = QUICK_ORDER_REMOVE_ITEM_PAGE_URL, method = RequestMethod.POST)
	@RequireHardLogIn
	public String quickOrderRemoveItem(final Model model, @RequestParam("energizerMaterialID") final String energizerMaterialID,
			final HttpSession session) throws CMSItemNotFoundException
	{
		final QuickOrderData quickOrder = quickOrderFacade.getQuickOrderFromSession((QuickOrderData) session
				.getAttribute(EnergizerQuickOrderFacade.QUICK_ORDER_SESSION_ATTRIBUTE));
		quickOrderFacade.removeItemFromQuickOrder(quickOrder, energizerMaterialID);

		session.setAttribute(EnergizerQuickOrderFacade.QUICK_ORDER_SESSION_ATTRIBUTE, quickOrder);
		model.addAttribute("orderform", quickOrder);

		storeCmsPageInModel(model, getContentPageForLabelOrId(QUICK_ORDER_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(QUICK_ORDER_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs(null));
		model.addAttribute("metaRobots", "no-index,no-follow");

		//return ControllerConstants.Views.Pages.Account.AccountQuickOrderPage;
		return REDIRECT_MY_ACCOUNT_QUICK_ORDER;
	}

	@RequestMapping(value = QUICK_ORDER_RESET_PAGE_URL, method = RequestMethod.GET)
	@RequireHardLogIn
	public String quickOrderReset(final Model model, final HttpSession session) throws CMSItemNotFoundException
	{
		final QuickOrderData quickOrder = quickOrderFacade.getQuickOrderFromSession(null);

		session.setAttribute(EnergizerQuickOrderFacade.QUICK_ORDER_SESSION_ATTRIBUTE, quickOrder);
		model.addAttribute("orderform", quickOrder);

		storeCmsPageInModel(model, getContentPageForLabelOrId(QUICK_ORDER_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(QUICK_ORDER_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs(null));
		model.addAttribute("metaRobots", "no-index,no-follow");

		//return ControllerConstants.Views.Pages.Account.AccountQuickOrderPage;
		return REDIRECT_MY_ACCOUNT_QUICK_ORDER;
	}

	@RequestMapping(value = QUICK_ORDER_ADD_TO_CART_PAGE_URL, method = RequestMethod.GET)
	@RequireHardLogIn
	public String quickOrderAddToCartAndContinue(final Model model, final HttpSession session) throws CMSItemNotFoundException
	{
		QuickOrderData quickOrder = quickOrderFacade.getQuickOrderFromSession((QuickOrderData) session
				.getAttribute(EnergizerQuickOrderFacade.QUICK_ORDER_SESSION_ATTRIBUTE));

		if (quickOrder.getLineItems() != null && quickOrder.getLineItems().size() > 0)
		{
			cartFacade.addOrderEntryList(quickOrder.getLineItems());

			//reset the quick order data once added to cart
			quickOrder = quickOrderFacade.getQuickOrderFromSession(null);
		}

		session.setAttribute(EnergizerQuickOrderFacade.QUICK_ORDER_SESSION_ATTRIBUTE, quickOrder);


		return REDIRECT_QUICK_ORDER_TO_CART;
	}

	@RequestMapping(value = "/excelfileupload", method = RequestMethod.GET)
	@RequireHardLogIn
	public String redirectToUploadFile(final Model model) throws CMSItemNotFoundException
	{
		storeCmsPageInModel(model, getContentPageForLabelOrId(EXCEL_UPLOAD_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(EXCEL_UPLOAD_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.excelFileUpload"));
		model.addAttribute("metaRobots", "no-index,no-follow");
		return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
	}

	@RequestMapping(value = QUICK_ORDER_AJAX_UPDATE_URL, method = RequestMethod.GET)
	@RequireHardLogIn
	public @ResponseBody
	QuickOrderData quickOrderAjaxOrderUpdate(final Model model, @RequestParam("productCode") final String productCode,
			@RequestParam("qty") final Long qty, final HttpSession session) throws CMSItemNotFoundException
	{
		final QuickOrderData quickOrder = quickOrderFacade.getQuickOrderFromSession((QuickOrderData) session
				.getAttribute(EnergizerQuickOrderFacade.QUICK_ORDER_SESSION_ATTRIBUTE));

		if (quickOrder.getLineItems() != null && quickOrder.getLineItems().size() > 0)
		{

			quickOrderFacade.updateQtyToExistingProduct(quickOrder, productCode, qty);
		}

		session.setAttribute(EnergizerQuickOrderFacade.QUICK_ORDER_SESSION_ATTRIBUTE, quickOrder);


		return quickOrder;
	}

}
