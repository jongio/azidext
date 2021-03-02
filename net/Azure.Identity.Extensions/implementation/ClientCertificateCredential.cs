// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

using Azure.Core;
using Azure.Core.Pipeline;
using Microsoft.Identity.Client;
using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
namespace Azure.Identity.Extensions.implementation
{
    class ClientCertificateCredential
    {
        internal interface IX509Certificate2Provider
        {
            ValueTask<X509Certificate2> GetCertificateAsync(bool async, CancellationToken cancellationToken);
        }

        /// <summary>
        /// X509Certificate2FromObjectProvider provides an X509Certificate2 from an existing instance.
        /// </summary>
        private class X509Certificate2FromObjectProvider : IX509Certificate2Provider
        {
            private X509Certificate2 Certificate { get; }

            public X509Certificate2FromObjectProvider(X509Certificate2 clientCertificate)
            {
                Certificate = clientCertificate ?? throw new ArgumentNullException(nameof(clientCertificate));
            }

            public ValueTask<X509Certificate2> GetCertificateAsync(bool async, CancellationToken cancellationToken)
            {
                return new ValueTask<X509Certificate2>(Certificate);
            }
        }
    }
}
