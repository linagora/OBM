-- ////////////////////////////////////////////////////////////////////////////
-- // Clean OBM Database from 0.4.x to 0.5 after Exporting DATA              //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////

-- Remove tables *Display (now handled by DisplayPref)
DROP table IF EXISTS AccountDisplay;
DROP table IF EXISTS CompanyDisplay;
DROP table IF EXISTS ComputerDisplay;
DROP table IF EXISTS ContactDisplay;
DROP table IF EXISTS DealDisplay;
DROP table IF EXISTS InvoiceDisplay;
DROP table IF EXISTS ListDisplay;
DROP table IF EXISTS ParentDealDisplay;
DROP table IF EXISTS PaymentDisplay;
