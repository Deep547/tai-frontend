/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.tai.service.journeyCache

import uk.gov.hmrc.tai.connectors.JourneyCacheConnector
import uk.gov.hmrc.tai.util.constants.JourneyCacheConstants._
import uk.gov.hmrc.tai.util.constants.BankAccountDecisionConstants._
import uk.gov.hmrc.tai.util.constants.journeyCache.UpdateNextYearsIncomeConstants

class AddEmploymentJourneyCacheService extends JourneyCacheService(AddEmployment_JourneyKey, JourneyCacheConnector)
class AddPensionProviderJourneyCacheService extends JourneyCacheService(AddPensionProvider_JourneyKey, JourneyCacheConnector)
class CloseBankAccountJourneyCacheService extends JourneyCacheService(CloseBankAccountJourneyKey, JourneyCacheConnector)
class CompanyCarJourneyCacheService extends JourneyCacheService(CompanyCar_JourneyKey, JourneyCacheConnector)
class EndCompanyBenefitJourneyCacheService extends JourneyCacheService(EndCompanyBenefit_JourneyKey, JourneyCacheConnector)
class EndEmploymentJourneyCacheService extends JourneyCacheService(EndEmployment_JourneyKey, JourneyCacheConnector)
class TrackSuccessfulJourneyJourneyCacheService extends JourneyCacheService(TrackSuccessfulJourney_JourneyKey, JourneyCacheConnector)
class UpdateBankAccountJourneyCacheService extends JourneyCacheService(UpdateBankAccountJourneyKey, JourneyCacheConnector)
class UpdateBankAccountChoiceJourneyCacheService extends JourneyCacheService(UpdateBankAccountChoiceJourneyKey, JourneyCacheConnector)
class UpdateEmploymentJourneyCacheService extends JourneyCacheService(UpdateEmployment_JourneyKey, JourneyCacheConnector)
class UpdateIncomeJourneyCacheService extends JourneyCacheService(UpdateIncome_JourneyKey, JourneyCacheConnector)
class UpdateNextYearsIncomeJourneyCacheService extends JourneyCacheService(UpdateNextYearsIncomeConstants.JOURNEY_KEY, JourneyCacheConnector)
class UpdatePensionProviderJourneyCacheService extends JourneyCacheService(UpdatePensionProvider_JourneyKey, JourneyCacheConnector)
class UpdatePreviousYearsIncomeJourneyCacheService extends JourneyCacheService(UpdatePreviousYearsIncome_JourneyKey, JourneyCacheConnector)
