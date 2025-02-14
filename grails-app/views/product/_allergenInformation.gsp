<div>
	<%
		def isInArray = { arrayValues, valueToCheck ->
			if (arrayValues != null ) {
				return arrayValues.contains(valueToCheck)

			}
			return false;
		}

		def entriesPerColumn = (int) ((AllergenDefinitions.size() + 1) / 2)

		AllergenDefinitions.sort { a, b -> a.name <=> b.name }
	%>

	<div class="container">
		<div class="row">
			%{--Left Column--}%
			<div class="col-12 col-lg-6 mt-1">
				<g:each in="${0..<entriesPerColumn}" var="index">
					<div class="row form-group align-items-center">
						<div class="col-6 text-right">
							<label for="Allergen-Label-${index}"
								   class="col-form-label"
								   style="white-space: normal; text-align: right; display: inline-block; word-break: break-word; overflow-wrap: anywhere;max-width: 100%; min-width: 100%;">
								${AllergenDefinitions[index].name}
							</label>
						</div>
						<div class="col-6">
							<div class="form-check d-flex align-items-center pl-0">
								<g:checkBox id="allergenSelection-${index}"
											name="allergenIds"
											value="${AllergenDefinitions[index].id}"
											checked="${isInArray(AllergenProductSelections,AllergenDefinitions[index].id)==true}"
											class="col-lg-12 form-check-input wl-checkbox"
											disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"
											style="margin-left: 0; margin-top: 0;"/>
							</div>
						</div>
					</div>
				</g:each>
			</div>
			%{--Right Column--}%
			<div class="col-12 col-lg-6 mt-1">
				<g:each in="${entriesPerColumn..<AllergenDefinitions.size()}" var="index">
					<div class="row form-group align-items-center">
						<div class="col-6 text-right">
							<label for="Allergen-Label-${index}"
								   class="col-form-label wl-label"
								   style="white-space: normal; text-align: right; display: inline-block; word-break: break-word; overflow-wrap: anywhere;max-width: 100%; min-width: 100%;">
								${AllergenDefinitions[index].name}
							</label>
						</div>
						<div class="col-6">
							<div class="form-check d-flex align-items-center pl-0">
								<g:checkBox id="allergenSelection-${index}"
											name="allergenIds"
											value="${AllergenDefinitions[index].id}"
											checked="${isInArray(AllergenProductSelections,AllergenDefinitions[index].id)==true}"
											class="col-lg-12 form-check-input wl-checkbox"
											disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"
											style="margin-left: 0; margin-top: 0;"/>
							</div>
						</div>
					</div>
				</g:each>
			</div>
		</div>
	</div>
</div>