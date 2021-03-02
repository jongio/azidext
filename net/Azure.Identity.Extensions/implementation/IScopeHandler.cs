// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

using System;

namespace Azure.Identity.Extensions.implementation
{
    internal interface IScopeHandler
    {
        DiagnosticScope CreateScope(ClientDiagnostics diagnostics, string name);
        void Start(string name, in DiagnosticScope scope);
        void Dispose(string name, in DiagnosticScope scope);
        void Fail(string name, in DiagnosticScope scope, Exception exception);
    }
}
