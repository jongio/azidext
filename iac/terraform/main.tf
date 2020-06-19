resource "azurerm_resource_group" "rg" {
  name     = "${var.basename}rg"
  location = var.location
}

resource "azurerm_servicebus_namespace" "sbns" {
  name                = "${var.basename}sbns"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  sku                 = "Standard"
}

resource "azurerm_servicebus_topic" "sbtopic" {
  name                = "topic1"
  resource_group_name = azurerm_resource_group.rg.name
  namespace_name      = azurerm_servicebus_namespace.sbns.name
}

resource "azurerm_servicebus_subscription" "sbsub" {
  name                = "sub1"
  resource_group_name = azurerm_resource_group.rg.name
  namespace_name      = azurerm_servicebus_namespace.sbns.name
  topic_name          = azurerm_servicebus_topic.sbtopic.name
  max_delivery_count  = 1
}

output "sb_connection_string" {
  value = azurerm_servicebus_namespace.sbns.default_primary_connection_string
}
