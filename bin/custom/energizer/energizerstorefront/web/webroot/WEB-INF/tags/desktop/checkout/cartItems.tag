<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="cartData" required="true" type="de.hybris.platform.commercefacades.order.data.CartData" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="grid" tagdir="/WEB-INF/tags/desktop/grid" %>


<div id="cartItems" class="clear">
	<div class="headline">
		<spring:theme code="basket.page.title.yourItems"/>
		<span class="cartId">
			<spring:theme code="basket.page.cartId"/>&nbsp;<span class="cartIdNr">${cartData.code}</span>
		</span>
	</div>
		
	<table class="cart">
		<thead>
			<tr>
				<th id="header2" colspan="2"><spring:theme code="basket.page.title"/></th>
				
				<th id="header3"><spring:theme code="basket.page.prdCode"/></th>				
				<th id="header5"><spring:theme code="basket.page.cmirId"/></th>
				<th id="header6"><spring:theme code="basket.page.shipFrom"/></th>
				<th id="header7"><spring:theme code="basket.page.uom"/></th>
								
				<th id="header8"><spring:theme code="basket.page.unitPrice"/></th>
				<th id="header9"><spring:theme code="basket.page.quantity"/></th>
				<th id="header10"><spring:theme code="basket.page.total"/></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${cartData.entries}" var="entry">
				<c:url value="${entry.product.url}" var="productUrl"/>
				<tr class="cartItem">
					
					<td headers="header2" class="thumb">
						<a href="${productUrl}"><product:productPrimaryImage product="${entry.product}" format="thumbnail"/></a>
					</td>
					
					<td headers="header2" class="details">
						<ycommerce:testId code="cart_product_name">
							<div class="itemName"><a href="${productUrl}">${entry.product.name}</a></div>
						</ycommerce:testId>
						
						<c:set var="entryStock" value="${entry.product.stock.stockLevelStatus.code}"/>
						
						<c:forEach items="${entry.product.baseOptions}" var="option">
							<c:if test="${not empty option.selected and option.selected.url eq entry.product.url}">
								<c:forEach items="${option.selected.variantOptionQualifiers}" var="selectedOption">
									<div>
										<strong>${selectedOption.name}:</strong>
										<span>${selectedOption.value}</span>
									</div>
									<c:set var="entryStock" value="${option.selected.stock.stockLevelStatus.code}"/>
									<div class="clear"></div>
								</c:forEach>
							</c:if>
						</c:forEach>
						
						<c:if test="${ycommerce:doesPotentialPromotionExistForOrderEntry(cartData, entry.entryNumber)}">
							<ul class="potentialPromotions">
								<c:forEach items="${cartData.potentialProductPromotions}" var="promotion">
									<c:set var="displayed" value="false"/>
									<c:forEach items="${promotion.consumedEntries}" var="consumedEntry">
										<c:if test="${not displayed && consumedEntry.orderEntryNumber == entry.entryNumber && not empty promotion.description}">
											<c:set var="displayed" value="true"/>
											<li>
												<ycommerce:testId code="cart_potentialPromotion_label">
													${promotion.description}
												</ycommerce:testId>
											</li>
										</c:if>
									</c:forEach>
								</c:forEach>
							</ul>
						</c:if>
								
						<c:if test="${ycommerce:doesAppliedPromotionExistForOrderEntry(cartData, entry.entryNumber)}">
							<ul class="appliedPromotions">
								<c:forEach items="${cartData.appliedProductPromotions}" var="promotion">
									<c:set var="displayed" value="false"/>
									<c:forEach items="${promotion.consumedEntries}" var="consumedEntry">
										<c:if test="${not displayed && consumedEntry.orderEntryNumber == entry.entryNumber}">
											<c:set var="displayed" value="true"/>
											<li>
												<ycommerce:testId code="cart_appliedPromotion_label">
													${promotion.description}
												</ycommerce:testId>
											</li>
										</c:if>
									</c:forEach>
								</c:forEach>
							</ul>
						</c:if>
					</td>					
					<td headers="header3" class="prdCode">
					 ${entry.product.erpMaterialID}
					</td>					
					
					<td headers="header5" class="cmirId">
					  ${entry.product.customerMaterialId}
					</td>
					
					<td headers="header6" class="shipFrom">
					 ${entry.product.shippingPoint}
					</td>
					
					<td headers="header7" class="uom">
					 ${entry.product.uom}
					</td>
				
					<td headers="header8" class="itemPrice">
						<c:choose>
							<c:when test="${entry.product.multidimensional and (entry.product.priceRange.minPrice.value ne entry.product.priceRange.maxPrice.value)}" >
								<format:price priceData="${entry.product.priceRange.minPrice}" displayFreeForZero="true"/>
								-
								<format:price priceData="${entry.product.priceRange.maxPrice}" displayFreeForZero="true"/>
							</c:when>
							<c:otherwise>
								<format:price priceData="${entry.basePrice}" displayFreeForZero="true"/>
							</c:otherwise>
						</c:choose>
					</td>

					<td headers="header9" class="quantity"><input type="hidden"
						name="entryNumber" value="${entry.entryNumber}" /> <input
						type="hidden" name="productCode" value="${entry.product.code}" />
						<input type="hidden" name="initialQuantity"	value="${entry.quantity}" />
						 <span class="qty"> <c:out	value="${entry.quantity}" /></span>
					</td>

					<td headers="header10" class="total">
						<ycommerce:testId code="cart_totalProductPrice_label">
							<format:price priceData="${entry.totalPrice}" displayFreeForZero="true"/>
						</ycommerce:testId>
					</td>				 
				</tr>
				 
				<c:if test="${entry.product.multidimensional}" >
					<tr><th colspan="5">
						<c:forEach items="${entry.entries}" var="currentEntry" varStatus="stat">
							<c:set var="subEntries" value="${stat.first ? '' : subEntries}${currentEntry.product.code}:${currentEntry.quantity},"/>
						</c:forEach>

						<div style="display:none" id="grid_${entry.product.code}" data-sub-entries="${subEntries}"> </div>
					</th></tr>		
				</c:if>
				 
			</c:forEach>
		</tbody>
	</table>
	
	<product:productOrderFormJQueryTemplates />
	
</div>

